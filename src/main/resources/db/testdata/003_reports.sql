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
('Aplikacja nie uruchamia się', 'Po aktualizacji systemu aplikacja przestała działać.', '2025-02-27 10:30:00', '2025-03-05 15:00:00', '2025-03-01 12:30:00', 1, 'PENDING', 1, 2),
('Błąd podczas logowania', 'Użytkownicy zgłaszają problemy z logowaniem do systemu.', '2025-03-02 09:00:00', '2025-03-10 12:00:00', '2025-03-02 10:00:00', 2, 'UNDER_REVIEW', 2, 3),
('Wolne ładowanie strony', 'Główna strona aplikacji ładuje się ponad 10 sekund.', '2025-03-03 14:45:00', '2025-03-08 18:00:00', '2025-03-03 16:00:00', 3, 'PENDING', 3, 4),
('Nowa funkcja - eksport danych', 'Potrzebna funkcja eksportu raportów do plików CSV.', '2025-03-04 11:15:00', '2025-03-10 20:30:00', '2025-03-04 13:15:00', 4, 'PENDING', 4, NULL),
('Luki bezpieczeństwa', 'Podejrzenie podatności XSS w formularzach.', '2025-03-05 13:20:00', '2025-03-12 14:00:00', '2025-03-05 15:20:00', 5, 'PENDING', 5, 1),
('Znikające dane w tabeli', 'Po odświeżeniu strony znikają rekordy w tabeli.', '2025-03-06 08:10:00', '2025-03-09 17:00:00', '2025-03-06 09:00:00', 6, 'COMPLETED', 1, 2),
('Awarie serwera', 'Serwer restartuje się bez ostrzeżenia.', '2025-03-07 16:30:00', '2025-09-15 09:00:00', '2026-08-15 17:00:00', 7, 'PENDING', 2, 3),
('Problem z responsywnością', 'Strona nie dostosowuje się poprawnie na urządzeniach mobilnych.', '2025-03-08 10:00:00', '2025-03-14 14:00:00', '2025-03-08 11:00:00', 8, 'UNDER_REVIEW', 3, 4),
('Dodanie ciemnego motywu', 'Sugestia dodania trybu ciemnego w aplikacji.', '2025-03-09 12:30:00', '2025-03-20 15:00:00', '2025-03-09 13:30:00', 7, 'PENDING', 4, NULL),
('Brak powiadomień email', 'System nie wysyła powiadomień o nowych zgłoszeniach.', '2025-03-10 17:00:00', '2025-03-18 19:30:00', '2025-03-10 18:00:00', 6, 'PENDING', 5, 1),
('Przekierowanie na błędną stronę', 'Po wylogowaniu użytkownik trafia na nieistniejącą stronę.', '2025-03-11 13:10:00', '2025-03-15 11:00:00', '2025-03-11 14:00:00', 5, 'PENDING', 1, 2),
('Błąd w kalkulatorze', 'System źle liczy sumę zamówień.', '2025-03-12 15:20:00', '2025-03-16 13:00:00', '2025-03-12 16:20:00', 3, 'UNDER_REVIEW', 2, 3),
('Nieczytelna czcionka', 'Na ekranach o wysokiej rozdzielczości czcionka jest zbyt mała.', '2025-03-13 09:50:00', '2025-03-17 16:00:00', '2025-03-13 10:30:00', 6, 'PENDING', 3, 4),
('Brak możliwości zmiany hasła', 'Użytkownicy nie mogą resetować swoich haseł.', '2025-03-14 14:00:00', '2025-03-19 10:30:00', '2025-03-14 15:00:00', 4, 'PENDING', 4, 1),
('Niepoprawne uprawnienia', 'Zwykły użytkownik ma dostęp do panelu administratora.', '2025-04-15 16:30:00', '2025-04-22 12:00:00', '2025-04-17 17:00:00', 5, 'COMPLETED', 5, 2);
