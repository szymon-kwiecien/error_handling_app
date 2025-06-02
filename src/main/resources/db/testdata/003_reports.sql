INSERT INTO report_category(name) VALUES
('Błąd krytyczny'),
('Błąd aplikacji'),
('Problem z wydajnością'),
('Sugestia ulepszenia'),
('Bezpieczeństwo'),
('Problemy z interfejsem'),
('Awaria systemu'),
('Inne');

INSERT INTO report (title, description, dated_added, due_date, time_to_respond, category_id, status, reporting_user_id, assigned_employee_id) VALUES
('Aplikacja nie uruchamia się', 'Po aktualizacji systemu aplikacja przestała działać.', '2025-05-28 10:30:00', '2025-06-08 15:00:00', '2025-06-04 20:30:00', 1, 'PENDING', 1, NULL),
('Błąd podczas logowania', 'Użytkownicy zgłaszają problemy z logowaniem do systemu.', '2025-05-27 09:00:00', '2025-06-07 12:00:00', '2025-06-03 10:00:00', 2, 'UNDER_REVIEW', 1, 2),
('Wolne ładowanie strony', 'Główna strona aplikacji ładuje się ponad 10 sekund.', '2025-05-27 14:45:00', '2025-06-09 18:00:00', '2025-06-04 16:00:00', 3, 'PENDING', 4, NULL),
('Nowa funkcja - eksport danych', 'Potrzebna funkcja eksportu raportów do plików CSV.', '2025-04-27 11:15:00', '2025-05-10 20:30:00', '2025-05-05 13:15:00', 4, 'PENDING', 4, NULL),
('Luki bezpieczeństwa', 'Podejrzenie podatności XSS w formularzach.', '2025-06-01 13:20:00', '2025-06-11 14:00:00', '2025-06-06 15:20:00', 5, 'PENDING', 1, NULL),
('Znikające dane w tabeli', 'Po odświeżeniu strony znikają rekordy w tabeli.', '2025-04-27 08:10:00', '2025-05-07 17:00:00', '2025-05-03 09:00:00', 6, 'COMPLETED', 1, 5),
('Awarie serwera', 'Serwer restartuje się bez ostrzeżenia.', '2025-04-28 16:30:00', '2025-05-13 09:00:00', '2025-05-06 17:00:00', 7, 'PENDING', 4, NULL),
('Problem z responsywnością', 'Strona nie dostosowuje się poprawnie na urządzeniach mobilnych.', '2025-05-26 10:00:00', '2025-06-12 14:00:00', '2025-06-04 11:00:00', 8, 'UNDER_REVIEW', 1, 2),
('Dodanie ciemnego motywu', 'Sugestia dodania trybu ciemnego w aplikacji.', '2025-05-29 12:30:00', '2025-06-09 15:00:00', '2025-06-05 13:30:00', 7, 'PENDING', 4, NULL),
('Brak powiadomień email', 'System nie wysyła powiadomień o nowych zgłoszeniach.', '2025-04-28 17:00:00', '2025-05-10 19:30:00', '2025-05-06 18:00:00', 6, 'PENDING', 1, NULL),
('Przekierowanie na błędną stronę', 'Po wylogowaniu użytkownik trafia na nieistniejącą stronę.', '2025-04-29 13:10:00', '2025-05-09 11:00:00', '2025-05-05 14:00:00', 5, 'PENDING', 1, NULL),
('Błąd w kalkulatorze', 'System źle liczy sumę zamówień.', '2025-05-31 15:20:00', '2025-06-08 13:00:00', '2025-06-03 16:20:00', 3, 'UNDER_REVIEW', 4, 3),
('Nieczytelna czcionka', 'Na ekranach o wysokiej rozdzielczości czcionka jest zbyt mała.', '2025-05-29 09:50:00', '2025-06-09 16:00:00', '2025-06-04 10:30:00', 6, 'PENDING', 1, NULL),
('Brak możliwości zmiany hasła', 'Użytkownicy nie mogą resetować swoich haseł.', '2025-05-29 14:00:00', '2025-06-07 10:30:00', '2025-06-03 15:00:00', 4, 'PENDING', 4, NULL),
('Niepoprawne uprawnienia', 'Zwykły użytkownik ma dostęp do panelu administratora.', '2025-04-29 16:30:00', '2025-05-11 12:00:00', '2025-05-06 17:00:00', 5, 'COMPLETED', 1, 5);
