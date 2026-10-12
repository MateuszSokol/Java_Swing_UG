# Rejestr zakupów komputerów — USD → PLN

Aplikacja okienkowa w Javie umożliwiająca rejestrowanie zakupów komputerów, przeliczanie ich ceny z USD na PLN według średniego kursu NBP dla wskazanej daty, zapis do MySQL oraz eksport do XML. Zapisane zakupy można przeglądać, filtrować i sortować w interfejsie Swing.

## Funkcjonalności

- Dodawanie komputera: nazwa, data księgowania i cena w USD.
- Pobranie kursu USD z tabeli A NBP przez HTTPS GET; odpowiedź JSON jest odczytywana przez Jacksona.
- Przy braku notowania dla wskazanej daty: wybór ostatniego dostępnego wcześniejszego kursu, maksymalnie 14 dni wstecz, i informacja o jego dacie w komunikacie po zapisie.
- Przeliczenie kwoty z użyciem `BigDecimal` i zaokrągleniem wyniku PLN do dwóch miejsc metodą `HALF_UP`.
- Zapis zakupu do MySQL i odczyt identyfikatora nadanego przez bazę.
- Automatyczny eksport wszystkich zapisanych komputerów do `reports/faktura.xml` po każdym dodaniu.
- Wyszukiwanie po fragmencie nazwy, dokładnej dacie lub obu kryteriach jednocześnie.
- Wyświetlanie ID, nazwy, daty księgowania oraz cen USD i PLN w tabeli.
- Sortowanie przez kliknięcie nagłówka kolumny, w tym po nazwie i dacie.
- Operacje sieciowe, bazodanowe i eksport związany z dodawaniem wykonywane w tle przez `SwingWorker`.

## Wymagania

- JDK 21 — wersja ustawiona w `pom.xml`.
- MySQL dostępny pod adresem `localhost:3306`.
- IntelliJ IDEA z obsługą Maven lub Maven dostępny w terminalu.
- Dostęp do internetu do pobrania zależności oraz kursów NBP.
- Uprawnienia do odczytu i zapisu w bazie oraz do tworzenia plików w katalogu roboczym aplikacji.

Zależności zadeklarowane w projekcie:

| Biblioteka | Wersja w `pom.xml` | Zastosowanie |
|---|---|---|
| `com.mysql:mysql-connector-j` | `26.7.0` | Sterownik JDBC MySQL |
| `com.fasterxml.jackson.core:jackson-databind` | `2.22.3` | Odczyt JSON |

Swing, JDBC i API StAX do zapisu XML pochodzą z JDK. Projekt nie wymaga Springa ani serwera aplikacyjnego. Nie zawiera skryptów Maven Wrapper; do uruchomienia poleceń `mvn` potrzebna jest instalacja Maven.

## Przygotowanie bazy danych

Uruchom MySQL i wykonaj poniższy skrypt, np. w MySQL Workbench. Można wykonać go kontem administracyjnym, a aplikacji przekazać dane konta z uprawnieniami `SELECT` i `INSERT` do tabeli `computers`.

```sql
CREATE DATABASE IF NOT EXISTS computer_purchases
    CHARACTER SET utf8mb4;

USE computer_purchases;

CREATE TABLE IF NOT EXISTS computers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usd_price DECIMAL(12,2) NOT NULL,
    pln_price DECIMAL(12,2) NOT NULL,
    computer_name VARCHAR(100) NOT NULL,
    booking_date DATE NOT NULL
);
```

Skrypt nie usuwa istniejących danych. `IF NOT EXISTS` nie aktualizuje struktury istniejącej tabeli — jeśli utworzono ją wcześniej, należy sprawdzić zgodność kolumn.

| Kolumna | Znaczenie / odpowiednik w zadaniu |
|---|---|
| `id` | Identyfikator generowany przez bazę |
| `computer_name` | `nazwa` |
| `booking_date` | `data_ksiegowania` |
| `usd_price` | `koszt_USD` |
| `pln_price` | `koszt_PLN` |

Aplikacja nie tworzy bazy ani tabeli automatycznie. Adres połączenia zapisano w `DatabaseConnection`:

```text
jdbc:mysql://localhost:3306/computer_purchases
```

Przy innym hoście, porcie lub nazwie bazy należy zmienić ten adres w klasie i ponownie skompilować projekt.

## Uruchomienie w IntelliJ IDEA

1. Otwórz główny folder projektu albo jego plik `pom.xml` jako projekt Maven.
2. Ustaw Project SDK na JDK 21 i przeładuj projekt Maven, aby pobrać zależności.
3. Przygotuj bazę zgodnie z powyższym skryptem.
4. Utwórz konfigurację typu **Application** dla klasy `org.example.Main`.
5. W **Run → Edit Configurations → Environment variables** dodaj:

   | Zmienna | Wartość |
   |---|---|
   | `DB_USER` | Login użytkownika MySQL |
   | `DB_PASSWORD` | Hasło tego użytkownika |

6. Ustaw **Working directory** na główny folder projektu. Dzięki temu raport zostanie zapisany w jego podfolderze `reports`.
7. Uruchom konfigurację `Main`.

Danych logowania nie należy dołączać do przesyłanego projektu. Każdy użytkownik ustawia je we własnej konfiguracji uruchomienia.

### Alternatywnie: terminal Windows / PowerShell

W głównym katalogu projektu, przy dostępnych poleceniach `java` i `mvn`, ustaw zmienne dla bieżącej sesji:

```powershell
$env:DB_USER = 'twoj_uzytkownik'
$env:DB_PASSWORD = 'twoje_haslo'
mvn clean compile dependency:copy-dependencies
java -cp "target/classes;target/dependency/*" org.example.Main
```

Polecenie `java` uruchom po pomyślnym zakończeniu Maven. Podane wartości logowania są przykładowymi miejscami na własne dane. Projekt nie ma skonfigurowanego samodzielnego pliku JAR zawierającego wszystkie zależności; samo `java -jar` nie jest udokumentowanym sposobem uruchomienia.

## Obsługa aplikacji

### Dodawanie zakupu

1. W menu wybierz **DODAJ KOMPUTER**.
2. Podaj nazwę, datę w formacie `RRRR-MM-DD` oraz dodatnią cenę USD. Formularz akceptuje kropkę lub przecinek jako separator dziesiętny. Podawaj kwoty z maksymalnie dwoma miejscami po separatorze i nazwy do 100 znaków, zgodnie ze strukturą bazy.
3. Kliknij **Zatwierdź formularz**.
4. Aplikacja pobierze kurs, obliczy PLN, zapisze zakup do bazy i odtworzy raport XML ze wszystkich rekordów.
5. Po zakończeniu wyświetli nazwę zapisanego komputera, kwoty USD i PLN, datę księgowania oraz datę wykorzystanego kursu NBP. Jeśli zastosowano wcześniejsze notowanie, komunikat zawiera dodatkowe wyjaśnienie.

Przycisk zatwierdzenia i powrotu jest blokowany na czas operacji. Niepoprawna data, pusta nazwa lub niepoprawna/niedodatnia cena powodują komunikat bez uruchamiania zapisu. Aplikacja nie wykrywa powtórnych zakupów: ponowne zatwierdzenie tych samych danych tworzy kolejny rekord.

### Wyszukiwanie i sortowanie

1. W menu wybierz **WYSZUKAJ KOMPUTER**.
2. Podaj fragment nazwy, datę lub oba kryteria i kliknij **Wyszukaj**. Można też nacisnąć Enter w polu filtra.
3. Pozostaw oba pola puste, aby wyświetlić wszystkie komputery.
4. Kliknij nagłówek **Nazwa komputera** lub **Data księgowania**, aby sortować wyniki. Ponowne kliknięcie odwraca kolejność.

Filtrowanie po obu kryteriach oznacza ich jednoczesne spełnienie. Wielkość liter przy wyszukiwaniu nazwy zależy od ustawień porównywania tekstu w MySQL. Komórki tabeli nie są edytowalne. Przycisk **Powrót do menu** przywraca ekran główny. Okno można powiększyć, aby wygodniej oglądać wszystkie kolumny.

## Kurs NBP i daty

Przykładowe zapytanie dla ACER Aspire:

```http
GET https://api.nbp.pl/api/exchangerates/rates/A/USD/2026-07-03/
Accept: application/json
```

Serwis odczytuje pole `mid` wybranego notowania z tablicy `rates` i oblicza:

```text
koszt PLN = koszt USD × średni kurs USD
```

Data z formularza jest jednocześnie datą księgowania i datą żądanego notowania. Szczegóły API: [dokumentacja NBP](https://api.nbp.pl/).

### Obsługa dni bez opublikowanego kursu

Aplikacja najpierw pyta o kurs dla dokładnie wskazanej daty. Wyłącznie po odpowiedzi HTTP 404 wykonuje dodatkowe zapytanie o przedział od 14 dni przed datą księgowania do dnia ją poprzedzającego. Z odpowiedzi wybiera notowanie o najpóźniejszej dacie publikacji. Nie korzysta z kursów późniejszych niż data księgowania.

Przykładowo **12 lipca 2026 przypada w niedzielę**. Dla DELL Latitude aplikacja zachowuje datę księgowania `2026-07-12` w bazie i XML, a do przeliczenia stosuje ostatni dostępny wcześniejszy kurs. Po zapisie wyświetla rzeczywistą datę zastosowanego kursu i informację o użyciu wcześniejszego notowania.

Jest to przyjęte założenie funkcjonalne, ponieważ treść zadania nie określa postępowania w dni bez notowań. Wybierany jest najbliższy **wcześniejszy** kurs, a nie najbliższy kurs w dowolnym kierunku.

Jeśli w całym 14-dniowym przedziale nie ma notowania, operacja kończy się błędem bez zapisu zakupu. Inne błędy HTTP, problemy z połączeniem i niepoprawna odpowiedź nie uruchamiają zastępowania kursu. Serwis odrzuca również daty księgowania późniejsze niż bieżąca data.

Data faktycznie użytego kursu jest przechowywana w obiekcie `Computer` na potrzeby komunikatu po dodaniu. Nie jest zapisywana w tabeli MySQL ani w XML i nie jest dostępna po ponownym odczycie rekordu z bazy. Wyszukiwanie i sortowanie po dacie dotyczą daty księgowania.

## Raport XML

Raport jest zapisywany w UTF-8 pod ścieżką `reports/faktura.xml`, względem katalogu roboczego aplikacji. Folder powstaje automatycznie. Każdy udany eksport nadpisuje plik pełną listą komputerów odczytaną z bazy.

Przykład struktury z fikcyjnymi kwotami:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<faktura>
  <komputer>
    <nazwa>Komputer przykładowy</nazwa>
    <data_ksiegowania>2026-07-03</data_ksiegowania>
    <koszt_USD>100.00</koszt_USD>
    <koszt_PLN>400.00</koszt_PLN>
  </komputer>
</faktura>
```

Zapis bazy i pliku nie stanowi jednej transakcji. Jeżeli zapis do bazy się uda, ale eksport zakończy się błędem, zakup pozostaje w bazie; komunikat informuje o tym i podaje ID. Nie należy ponownie dodawać tego samego zakupu. Metoda `ComputerService.exportAllComputersToXml()` umożliwia ponowienie samego eksportu z kodu, ale nie ma dla niej osobnego przycisku. Nieudany eksport może pozostawić plik niepełny, ponieważ zapis odbywa się bezpośrednio do pliku docelowego.

Plik XML dołączony do projektu zawiera dane próbne. Nie jest importowany do bazy podczas uruchamiania i nie zastępuje jej kopii.

## Dane z zadania

| Komputer | Cena USD | Data księgowania |
|---|---:|---|
| ACER Aspire | 345,00 | 2026-07-03 |
| DELL Latitude | 543,00 | 2026-07-12 |
| HP Victus | 346,00 | 2026-07-15 |
| **Suma** | **1234,00** | |

Zakupy wprowadza się ręcznie przez formularz. Projekt nie zawiera automatycznego wypełniania bazy tym zestawem. Dla DELL obowiązuje opisana wyżej zasada ostatniego dostępnego wcześniejszego kursu; data księgowania pozostaje zgodna z zadaniem.

## Organizacja kodu

| Pakiet / klasa | Odpowiedzialność |
|---|---|
| `org.example.Main` | Uruchomienie okna na wątku Swing EDT |
| `gui` | Menu, formularz, tabela, przełączanie kart i przyciski |
| `model.Computer` | Dane zakupu, kwoty `BigDecimal`, data `LocalDate` |
| `api.ConnectionHandler` | HTTP GET, timeouty i odczyt odpowiedzi |
| `api.NbpResponse`, `api.NbpRate` | Obiekty odwzorowujące JSON NBP |
| `api.ComputerService` | Wybór kursu dla daty lub wcześniejszego notowania, przeliczenie, zapis i eksport |
| `dao` | Połączenie JDBC, zapis i wyszukiwanie w MySQL |
| `xml.InvoiceXmlExporter` | Generowanie XML przez StAX |

## Rozwiązywanie problemów

- **Błąd połączenia z bazą:** sprawdź działanie MySQL, port, istnienie bazy i tabeli, uprawnienia oraz zmienne środowiskowe konfiguracji, z której uruchamiasz aplikację.
- **Brak sterownika lub `ObjectMapper`:** przeładuj Maven i sprawdź, czy zależności z `pom.xml` zostały pobrane.
- **Błąd NBP:** sprawdź internet i datę notowania. Timeout połączenia i odczytu wynosi po 5 sekund. Po HTTP 404 aplikacja szuka wcześniejszego kursu w granicach 14 dni. Brak kursu również w tym zakresie przerywa zapis; aplikacja nie podstawia kursu zerowego.
- **Brak raportu w folderze projektu:** sprawdź katalog roboczy procesu oraz uprawnienia zapisu; raport powstaje dopiero po dodaniu zakupu.
- **Brak wyników:** usuń filtry, kliknij ponownie wyszukiwanie i upewnij się, że aplikacja korzysta z oczekiwanej bazy.

## Weryfikacja działania

Projekt nie zawiera automatycznych testów. Ręczna weryfikacja powinna obejmować:

1. Dodanie ACER Aspire i HP Victus z danymi z tabeli oraz porównanie danych w bazie i XML.
2. Dodanie DELL Latitude z datą `2026-07-12`: komunikat powinien pokazać wcześniejszą datę kursu, a baza i XML zachować datę księgowania `2026-07-12`.
3. Wyszukiwanie po fragmencie nazwy, dacie oraz obu kryteriach.
4. Sortowanie po nazwie i dacie w obu kierunkach.
5. Komunikaty dla błędnego formularza, przyszłej daty, niedostępnej bazy i błędu pobierania kursu.

Ręcznie potwierdzono zapis DELL z datą księgowania `2026-07-12` w bazie oraz wyświetlenie komunikatu o zastosowaniu wcześniejszego kursu. Nie jest to deklaracja przejścia wszystkich powyższych scenariuszy.
