DELIMITER //
CREATE PROCEDURE customer_list(IN cityOption CHAR(16))
BEGIN
    SELECT id, first_name, middle_initial, last_name, address, city, state, zip_code
    FROM customer
    WHERE city = cityOption;
END //
DELIMITER ;