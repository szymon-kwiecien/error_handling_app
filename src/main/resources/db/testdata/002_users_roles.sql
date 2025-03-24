INSERT INTO users (first_name, last_name, email, password, company_id) VALUES
 /* haslo123 */
('Jan', 'Kowalski', 'jan.kowalski@example.com', '$2a$12$nvsws8lbeeYNlQz.U0zSuuk5TbZiyLsUq3Qd9Meg8DLl6/2b4bx/K', 1),
/* PRACOWNIK,  bezpieczneHaslo! */
('Anna', 'Nowak', 'anna.nowak@example.com', '$2a$12$3N0FXP7.Vg2cJPhvLDO8jeM3jK84CHh8QphpBVsgotWvHgtZ7fB9C', 2),
/* ADMIN,  tajneHaslo1 */
('Piotr', 'Wiśniewski', 'admin@example.com', '$2a$12$tezxXugfvQ1FNxrAHwAlO.1f6WI9pRGY167/WkSjigQFAH7jjQV1K', 3),
/* testowehaslo123 */
('Maria', 'Dąbrowska', 'maria.dabrowska@example.com', '$2a$12$rolAqIClNICoNNxLpQWhKu6/E3xxuOjf7ZwZKvTSFPcg696EYe/cu', 4),
/* PRACOWNIK, 1234pass */
('Tomasz', 'Lewandowski', 'tomasz.lewandowski@example.com', '$2a$12$pVi.Ev6a1AY4/jyOuU/eoOR4JL8h4dWMMsmlVGIjC3pop7GFWRZZu', 5);

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
