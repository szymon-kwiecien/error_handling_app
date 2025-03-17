INSERT INTO users (first_name, last_name, email, password, company_id) VALUES
('Jan', 'Kowalski', 'jan.kowalski@example.com', 'haslo123', 1),
/* PRACOWNIK */
('Anna', 'Nowak', 'anna.nowak@example.com', 'bezpieczneHaslo!', 2),
/* ADMIN */
('Piotr', 'Wiśniewski', 'admin@example.com', 'tajneHaslo1', 3),
('Maria', 'Dąbrowska', 'maria.dabrowska@example.com', 'mojeSuperHaslo', 4),
/* PRACOWNIK */
('Tomasz', 'Lewandowski', 'tomasz.lewandowski@example.com', '1234pass', 5);

INSERT INTO user_role (name, description) VALUES
('USER', 'Zwykły użytkownik z podstawowymi uprawnieniami'),
('ADMINISTRATOR', 'Użytkownik z uprawnieniami administracyjnymi, pełny dostęp do wszystkich funkcji i ustawień'),
('EMPLOYEE', 'Użytkownik z uprawnieniami pracowniczymi do obsługi zdarzeń');

INSERT INTO
    user_roles (user_id, role_id)
VALUES
    (1, 1),
    (2, 3),
    (3, 2),
    (4,1),
    (5,3);
