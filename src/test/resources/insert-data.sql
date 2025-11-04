INSERT INTO ratings(menu_id)
values
    (1), (2), (3), (4), (5), (6), (7), (8), (10);

INSERT INTO reviews(menu_id, created_by, comment, rate, created_at)
values
    (1, 'UserOne', 'CommentOne', 5, '2024-03-14 10:23:54'),
    (4, 'Username', 'Comment', 5, '2024-03-14 11:23:54'),
    (5, 'Username', 'Comment', 4, '2024-03-15 12:23:54'),
    (6, 'Username', 'Comment', 3, '2024-03-16 13:23:54'),
    (7, 'Username', 'Comment', 2, '2024-03-17 14:23:54'),
    (8, 'Username', 'Comment', 1, '2024-03-18 15:23:54'),
    (10, 'User1', 'Comment', 5, '2024-03-14 11:23:54'),
    (10, 'User2', 'Comment', 4, '2024-03-15 12:23:54'),
    (10, 'User3', 'Comment', 3, '2024-03-16 13:23:54'),
    (10, 'User4', 'Comment', 2, '2024-03-17 14:23:54'),
    (10, 'User5', 'Comment', 1, '2024-03-18 15:23:54');

UPDATE ratings SET rate_five = rate_five + 1 WHERE menu_id = 1;
UPDATE ratings SET rate_five = rate_five + 1 WHERE menu_id = 10;
UPDATE ratings SET rate_four = rate_four + 1 WHERE menu_id = 10;
UPDATE ratings SET rate_three = rate_three + 1 WHERE menu_id = 10;
UPDATE ratings SET rate_two = rate_two + 1 WHERE menu_id = 10;
UPDATE ratings SET rate_one = rate_one + 1 WHERE menu_id = 10;
UPDATE ratings SET rate_five = rate_five + 1 WHERE menu_id = 4;
UPDATE ratings SET rate_four = rate_four + 1 WHERE menu_id = 5;
UPDATE ratings SET rate_three = rate_three + 1 WHERE menu_id = 6;
UPDATE ratings SET rate_two = rate_two + 1 WHERE menu_id = 7;
UPDATE ratings SET rate_one = rate_one + 1 WHERE menu_id = 8;