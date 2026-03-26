INSERT INTO users (first_name, last_name, email, password, company_id, is_active) VALUES
 /* haslo123 */
('Jan', 'Kowalski', 'jan.kowalski@example.com', '$2a$12$nvsws8lbeeYNlQz.U0zSuuk5TbZiyLsUq3Qd9Meg8DLl6/2b4bx/K', 1, TRUE),
/* PRACOWNIK,  bezpieczneHaslo! */
('Anna', 'Nowak', 'anna.nowak@example.com', '$2a$12$3N0FXP7.Vg2cJPhvLDO8jeM3jK84CHh8QphpBVsgotWvHgtZ7fB9C', 2, TRUE),
/* ADMIN,  tajneHaslo1 */
('Piotr', 'Wiśniewski', 'admin@example.com', '$2a$12$tezxXugfvQ1FNxrAHwAlO.1f6WI9pRGY167/WkSjigQFAH7jjQV1K', 3, TRUE),
/* testowehaslo123 */
('Maria', 'Dąbrowska', 'maria.dabrowska@example.com', '$2a$12$rolAqIClNICoNNxLpQWhKu6/E3xxuOjf7ZwZKvTSFPcg696EYe/cu', 4, TRUE),
/* PRACOWNIK, 1234pass */
('Tomasz', 'Lewandowski', 'tomasz.lewandowski@example.com', '$2a$12$pVi.Ev6a1AY4/jyOuU/eoOR4JL8h4dWMMsmlVGIjC3pop7GFWRZZu', 5, TRUE),
('Krzysztof', 'Mazur', 'krzysztof.mazur@example.com', '$2a$12$nvsws8lbeeYNlQz.U0zSuuk5TbZiyLsUq3Qd9Meg8DLl6/2b4bx/K', 1, TRUE),
('Agnieszka', 'Kaczmarek', 'agnieszka.kaczmarek@example.com', '$2a$12$3N0FXP7.Vg2cJPhvLDO8jeM3jK84CHh8QphpBVsgotWvHgtZ7fB9C', 2, TRUE),
('Paweł', 'Piotrowski', 'pawel.piotrowski@example.com', '$2a$12$tezxXugfvQ1FNxrAHwAlO.1f6WI9pRGY167/WkSjigQFAH7jjQV1K', 3, TRUE),
('Magdalena', 'Grabowska', 'magdalena.grabowska@example.com', '$2a$12$rolAqIClNICoNNxLpQWhKu6/E3xxuOjf7ZwZKvTSFPcg696EYe/cu', 4, TRUE),
('Michał', 'Zieliński', 'michal.zielinski@example.com', '$2a$12$pVi.Ev6a1AY4/jyOuU/eoOR4JL8h4dWMMsmlVGIjC3pop7GFWRZZu', 5, TRUE),
('Barbara', 'Szymańska', 'barbara.szymanska@example.com', '$2a$12$nvsws8lbeeYNlQz.U0zSuuk5TbZiyLsUq3Qd9Meg8DLl6/2b4bx/K', 1, TRUE),
('Grzegorz', 'Woźniak', 'grzegorz.wozniak@example.com', '$2a$12$3N0FXP7.Vg2cJPhvLDO8jeM3jK84CHh8QphpBVsgotWvHgtZ7fB9C', 2, TRUE),
('Ewa', 'Kamińska', 'ewa.kaminska@example.com', '$2a$12$tezxXugfvQ1FNxrAHwAlO.1f6WI9pRGY167/WkSjigQFAH7jjQV1K', 3, TRUE),
('Łukasz', 'Lewicki', 'lukasz.lewicki@example.com', '$2a$12$rolAqIClNICoNNxLpQWhKu6/E3xxuOjf7ZwZKvTSFPcg696EYe/cu', 4, TRUE),
('Natalia', 'Dudek', 'natalia.dudek@example.com', '$2a$12$pVi.Ev6a1AY4/jyOuU/eoOR4JL8h4dWMMsmlVGIjC3pop7GFWRZZu', 5, TRUE),
('Sebastian', 'Król', 'sebastian.krol@example.com', '$2a$12$nvsws8lbeeYNlQz.U0zSuuk5TbZiyLsUq3Qd9Meg8DLl6/2b4bx/K', 1, TRUE);

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
    (5,3),
    (6, 1),
    (7, 3),
    (8, 2),
    (9, 1),
    (10, 3),
    (11, 2),
    (12, 1),
    (13, 3),
    (14, 2),
    (15, 1),
    (16, 3);
