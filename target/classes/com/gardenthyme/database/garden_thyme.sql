-- =====================================================
-- CPSC 471 - Garden Thyme
-- =====================================================

DROP DATABASE IF EXISTS GardenDB;
CREATE DATABASE GardenDB;
USE GardenDB;

-- -----------------------------------------------------
-- USERS TABLE
-- -----------------------------------------------------
CREATE TABLE USERS (
    User_ID INT NOT NULL PRIMARY KEY,
    Username VARCHAR(50) NOT NULL UNIQUE,
    Email VARCHAR(100) NOT NULL UNIQUE,
    Password VARCHAR(100) NOT NULL
);

-- -----------------------------------------------------
-- ADMIN TABLE
-- -----------------------------------------------------
CREATE TABLE ADMIN (
    User_ID INT AUTO_INCREMENT PRIMARY KEY,
    FOREIGN KEY (User_ID) REFERENCES USERS(User_ID)
);

-- -----------------------------------------------------
-- GARDENER TABLE
-- -----------------------------------------------------
CREATE TABLE GARDENER (
    User_ID INT AUTO_INCREMENT PRIMARY KEY,
    FOREIGN KEY (User_ID) REFERENCES USERS(User_ID)
);

-- -----------------------------------------------------
-- SOIL TABLE
-- -----------------------------------------------------
CREATE TABLE SOIL (
    Soil_Type VARCHAR(50) PRIMARY KEY,
    pH FLOAT
);

-- -----------------------------------------------------
-- PLANT TABLE
-- -----------------------------------------------------
CREATE TABLE PLANT (
    Plant_ID INT AUTO_INCREMENT PRIMARY KEY,
    Scientific_Name VARCHAR(100),
    Common_Name VARCHAR(100),
    Growth_Instructions TEXT,
    Soil_Type VARCHAR(50),
    FOREIGN KEY (Soil_Type) REFERENCES SOIL(Soil_Type)
);

-- -----------------------------------------------------
-- WEATHER_DATA TABLE
-- -----------------------------------------------------
CREATE TABLE WEATHER_DATA (
    Plant_ID INT,
    Best_Month VARCHAR(20),
    Temperature FLOAT,
    Humidity FLOAT,
    Sunlight VARCHAR(50),
    PRIMARY KEY (Plant_ID, Best_Month),
    FOREIGN KEY (Plant_ID) REFERENCES PLANT(Plant_ID)
        ON DELETE CASCADE
);
-- -----------------------------------------------------
-- HARVEST_LOG TABLE
-- -----------------------------------------------------
CREATE TABLE HARVEST_LOG (
    ID INT AUTO_INCREMENT,
    Plant_ID INT,
    Harvest_Date DATE,
    Quantity INT,
    PRIMARY KEY (ID, Plant_ID),
    FOREIGN KEY (Plant_ID) REFERENCES PLANT(Plant_ID)
);

-- -----------------------------------------------------
-- GARDEN TABLE
-- -----------------------------------------------------
CREATE TABLE GARDEN (
    Garden_ID INT PRIMARY KEY,
    Garden_Name VARCHAR(100) NOT NULL
);

-- -----------------------------------------------------
-- GARDEN_BOX TABLE
-- -----------------------------------------------------
CREATE TABLE GARDEN_BOX (
    Garden_ID INT,
    Box_ID INT,
    Area FLOAT,
    Soil_Type VARCHAR(50),
    PRIMARY KEY (Garden_ID, Box_ID),
    FOREIGN KEY (Garden_ID) REFERENCES GARDEN(Garden_ID),
    FOREIGN KEY (Soil_Type) REFERENCES SOIL(Soil_Type)
);

-- -----------------------------------------------------
-- PLANTED_IN TABLE
-- -----------------------------------------------------
CREATE TABLE PLANTED_IN (
    Plant_ID INT,
    Garden_ID INT,
    Box_ID INT,
    PRIMARY KEY (Plant_ID, Garden_ID, Box_ID),
    FOREIGN KEY (Plant_ID) REFERENCES PLANT(Plant_ID),
    FOREIGN KEY (Garden_ID, Box_ID) REFERENCES GARDEN_BOX(Garden_ID, Box_ID)
);

-- -----------------------------------------------------
-- TASK TABLE
-- -----------------------------------------------------
CREATE TABLE TASK (
    Task_ID INT AUTO_INCREMENT,
    Plant_ID INT,
    Date DATE,
    Text VARCHAR(255),
    isCompleted BOOLEAN,
    Frequency INT,
    Repeat_Until DATE,
    PRIMARY KEY (Task_ID, Plant_ID),
    FOREIGN KEY (Plant_ID) REFERENCES PLANT(Plant_ID)
);

-- -----------------------------------------------------
-- ASSIGNED_TO TABLE
-- -----------------------------------------------------
CREATE TABLE ASSIGNED_TO (
    Plant_ID INT,
    Task_ID INT,
    User_ID INT,
    PRIMARY KEY (Plant_ID, Task_ID, User_ID),
    FOREIGN KEY (Plant_ID, Task_ID) REFERENCES TASK(Plant_ID, Task_ID),
    FOREIGN KEY (User_ID) REFERENCES USERS(User_ID)
);

-- -----------------------------------------------------
-- MANAGES TABLE
-- -----------------------------------------------------
CREATE TABLE MANAGES (
    User_ID INT,
    Garden_ID INT,
    IsOwner BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (User_ID, Garden_ID),
    FOREIGN KEY (User_ID) REFERENCES USERS(User_ID),
    FOREIGN KEY (Garden_ID) REFERENCES GARDEN(Garden_ID)
);

-- -----------------------------------------------------
-- PESTS TABLE
-- -----------------------------------------------------
CREATE TABLE PESTS (
    Name VARCHAR(100) PRIMARY KEY,
    Effective_Pesticides TEXT
);

-- -----------------------------------------------------
-- TARGET_PLANTS TABLE
-- -----------------------------------------------------
CREATE TABLE TARGET_PLANTS (
    Pest_Name VARCHAR(100),
    Pest_Target_Plants VARCHAR(100),
    PRIMARY KEY (Pest_Name, Pest_Target_Plants),
    FOREIGN KEY (Pest_Name) REFERENCES PESTS(Name)
);

-- -----------------------------------------------------
-- SIGNS TABLE
-- -----------------------------------------------------
CREATE TABLE SIGNS (
    Pest_Name VARCHAR(100),
    Pest_Signs VARCHAR(255),
    PRIMARY KEY (Pest_Name, Pest_Signs),
    FOREIGN KEY (Pest_Name) REFERENCES PESTS(Name)
);

-- -----------------------------------------------------
-- INFECTING TABLE
-- -----------------------------------------------------
CREATE TABLE INFECTING (
    Pest_Name VARCHAR(100),
    Plant_ID INT,
    PRIMARY KEY (Pest_Name, Plant_ID),
    FOREIGN KEY (Pest_Name) REFERENCES PESTS(Name),
    FOREIGN KEY (Plant_ID) REFERENCES PLANT(Plant_ID)
);

-- -----------------------------------------------------
-- INFECTED_BY TABLE
-- -----------------------------------------------------
CREATE TABLE INFECTED_BY (
    Plant_ID INT,
    Pest_Name VARCHAR(100),
    PRIMARY KEY (Plant_ID, Pest_Name),
    FOREIGN KEY (Plant_ID) REFERENCES PLANT(Plant_ID),
    FOREIGN KEY (Pest_Name) REFERENCES PESTS(Name)
);

-- -----------------------------------------------------
-- ADMIN_PEST_ACTIONS TABLE
-- -----------------------------------------------------
CREATE TABLE ADMIN_PEST_ACTIONS (
    User_ID INT,
    Pest_Name VARCHAR(100),
    PRIMARY KEY (User_ID, Pest_Name),
    FOREIGN KEY (User_ID) REFERENCES ADMIN(User_ID),
    FOREIGN KEY (Pest_Name) REFERENCES PESTS(Name)
);

-- -----------------------------------------------------
-- ADMIN_PLANT_ACTIONS TABLE
-- -----------------------------------------------------
CREATE TABLE ADMIN_PLANT_ACTIONS (
    User_ID INT,
    Plant_ID INT,
    PRIMARY KEY (User_ID, Plant_ID),
    FOREIGN KEY (User_ID) REFERENCES ADMIN(User_ID),
    FOREIGN KEY (Plant_ID) REFERENCES PLANT(Plant_ID)
);

-- -----------------------------------------------------
-- ADDITIONAL SQL STATEMENTS
-- -----------------------------------------------------
INSERT INTO SOIL VALUES 
('Loam', 6.5),
('Clay', 5.8),
('Sandy', 6.0);

-- Users (password: password123 for all)
INSERT INTO USERS VALUES
(1, 'admin_garden', 'admin@garden.com', 'password123'),
(2, 'green_thumb', 'gardener1@garden.com', 'password123'),
(3, 'plant_master', 'gardener2@garden.com', 'password123'),
(4, 'flower_lover', 'flower@lover.com', 'password123'),
(5, 'veg_grower', 'veg@grower.com', 'password123');

INSERT INTO ADMIN VALUES (1);

INSERT INTO GARDENER VALUES
(2),
(3),
(4),
(5);

INSERT INTO GARDEN VALUES
(1, 'My Vegetable Garden'),
(2, 'Herb Garden'),
(3, 'Flower Paradise'),
(4, 'Community Garden');

-- Garden 1: User 1 is owner, User 2 and 3 are managers
-- Garden 2: User 1 is owner, User 4 is manager
-- Garden 3: User 2 is owner, User 5 is manager
-- Garden 4: User 3 is owner
INSERT INTO MANAGES VALUES
(1, 1, TRUE),   -- User 1 owns Garden 1
(2, 1, FALSE),  -- User 2 manages Garden 1
(3, 1, FALSE),  -- User 3 manages Garden 1
(1, 2, TRUE),   -- User 1 owns Garden 2
(4, 2, FALSE),  -- User 4 manages Garden 2
(2, 3, TRUE),   -- User 2 owns Garden 3
(5, 3, FALSE),  -- User 5 manages Garden 3
(3, 4, TRUE);   -- User 3 owns Garden 4

INSERT INTO GARDEN_BOX VALUES
(1, 1, 10.5, 'Loam'),
(1, 2, 8.0, 'Clay'),
(1, 3, 12.0, 'Sandy'),
(2, 1, 6.0, 'Loam'),
(2, 2, 5.5, 'Sandy'),
(3, 1, 15.0, 'Loam'),
(3, 2, 10.0, 'Clay'),
(4, 1, 20.0, 'Loam'),
(4, 2, 15.0, 'Sandy'),
(4, 3, 12.0, 'Clay');

INSERT INTO PLANT VALUES
(1, 'Solanum lycopersicum', 'Tomato', 'Water regularly, full sun needed', 'Loam'),
(2, 'Daucus carota', 'Carrot', 'Keep soil moist, partial sun', 'Sandy'),
(3, 'Lactuca sativa', 'Lettuce', 'Partial sunlight, keep cool', 'Loam'),
(4, 'Ocimum basilicum', 'Basil', 'Full sun, water when soil is dry', 'Loam'),
(5, 'Rosmarinus officinalis', 'Rosemary', 'Well-drained soil, full sun', 'Sandy'),
(6, 'Rosa spp', 'Rose', 'Full sun, regular watering', 'Clay'),
(7, 'Tagetes erecta', 'Marigold', 'Full sun, well-drained soil', 'Loam'),
(8, 'Capsicum annuum', 'Bell Pepper', 'Full sun, regular watering', 'Loam'),
(9, 'Cucumis sativus', 'Cucumber', 'Full sun, trellis recommended', 'Loam'),
(10, 'Fragaria ananassa', 'Strawberry', 'Full sun, well-drained soil', 'Sandy');

-- Planted In (plants in garden boxes)
-- Garden 1: Vegetable Garden
INSERT INTO PLANTED_IN VALUES
(1, 1, 1),  -- Tomato in Garden 1 Box 1
(2, 1, 2),  -- Carrot in Garden 1 Box 2
(3, 1, 2),  -- Lettuce in Garden 1 Box 2
(8, 1, 1),  -- Bell Pepper in Garden 1 Box 1
(9, 1, 3),  -- Cucumber in Garden 1 Box 3

-- Garden 2: Herb Garden
(4, 2, 1),  -- Basil in Garden 2 Box 1
(5, 2, 2),  -- Rosemary in Garden 2 Box 2

-- Garden 3: Flower Paradise
(6, 3, 1),  -- Rose in Garden 3 Box 1
(7, 3, 2),  -- Marigold in Garden 3 Box 2
(10, 3, 2), -- Strawberry in Garden 3 Box 2

-- Garden 4: Community Garden
(1, 4, 1),  -- Tomato in Garden 4 Box 1
(4, 4, 2),  -- Basil in Garden 4 Box 2
(7, 4, 3);  -- Marigold in Garden 4 Box 3

INSERT INTO WEATHER_DATA VALUES
(1, 'July', 25, 60, 'Full Sun'),
(2, 'June', 20, 55, 'Partial Sun'),
(3, 'May', 18, 65, 'Partial Sun'),
(4, 'July', 26, 58, 'Full Sun'),
(5, 'August', 28, 50, 'Full Sun'),
(6, 'June', 22, 60, 'Full Sun'),
(7, 'July', 24, 55, 'Full Sun'),
(8, 'August', 26, 58, 'Full Sun'),
(9, 'July', 25, 60, 'Full Sun'),
(10, 'June', 20, 55, 'Full Sun');

INSERT INTO HARVEST_LOG VALUES
(1, 1, '2026-08-10', 5),
(2, 1, '2026-07-15', 10),
(3, 1, '2026-06-20', 3),
(4, 1, '2026-07-25', 8),
(8, 1, '2026-08-05', 4);

INSERT INTO TASK VALUES
(1, 1, '2026-05-10', 'Water tomato plants', FALSE, 1, '2026-09-01'),
(2, 2, '2026-05-12', 'Check carrot soil moisture', FALSE, 7, '2026-08-01'),
(3, 3, '2026-05-15', 'Remove weeds around lettuce', FALSE, 7, '2026-07-30'),
(4, 4, '2026-05-10', 'Prune basil for better growth', FALSE, 7, '2026-08-01'),
(5, 5, '2026-05-10', 'Water rosemary sparingly', FALSE, 14, '2026-09-01'),
(6, 6, '2026-05-15', 'Apply rose fertilizer', FALSE, 30, '2026-08-15'),
(7, 7, '2026-05-12', 'Deadhead marigolds', FALSE, 7, '2026-08-01'),
(8, 8, '2026-05-10', 'Support bell pepper stems', FALSE, 7, '2026-08-01'),
(9, 9, '2026-05-10', 'Train cucumber on trellis', FALSE, 7, '2026-08-01'),
(10, 10, '2026-05-12', 'Mulch around strawberries', FALSE, 7, '2026-07-30');

INSERT INTO ASSIGNED_TO VALUES
(1, 1, 2),
(2, 2, 3),
(3, 3, 2),
(4, 4, 4),
(5, 5, 5),
(6, 6, 2),
(7, 7, 5),
(8, 8, 2),
(9, 9, 3),
(10, 10, 4);

INSERT INTO PESTS VALUES
('Aphids', 'Neem Oil, Insecticidal Soap'),
('Caterpillars', 'Bt Spray, Hand picking'),
('Spider Mites', 'Insecticidal Soap, Neem Oil'),
('Whiteflies', 'Yellow sticky traps, Insecticidal soap'),
('Slugs', 'Beer traps, Diatomaceous earth');

INSERT INTO TARGET_PLANTS VALUES
('Aphids', 'Tomato'),
('Aphids', 'Rose'),
('Caterpillars', 'Lettuce'),
('Caterpillars', 'Basil'),
('Spider Mites', 'Carrot'),
('Whiteflies', 'Tomato'),
('Whiteflies', 'Basil'),
('Slugs', 'Lettuce'),
('Slugs', 'Strawberry');

INSERT INTO SIGNS VALUES
('Aphids', 'Sticky residue on leaves'),
('Aphids', 'Curled or yellowing leaves'),
('Caterpillars', 'Holes in leaves'),
('Caterpillars', 'Chewed edges'),
('Spider Mites', 'Tiny webs under leaves'),
('Spider Mites', 'Speckled leaves'),
('Whiteflies', 'White flying insects when disturbed'),
('Whiteflies', 'Yellowing leaves'),
('Slugs', 'Irregular holes in leaves'),
('Slugs', 'Slime trails');

INSERT INTO INFECTING VALUES
('Aphids', 1),
('Aphids', 6),
('Caterpillars', 3),
('Caterpillars', 4),
('Spider Mites', 2),
('Whiteflies', 1),
('Whiteflies', 4),
('Slugs', 3),
('Slugs', 10);

INSERT INTO INFECTED_BY VALUES
(1, 'Aphids'),
(6, 'Aphids'),
(3, 'Caterpillars'),
(4, 'Caterpillars'),
(2, 'Spider Mites'),
(1, 'Whiteflies'),
(4, 'Whiteflies'),
(3, 'Slugs'),
(10, 'Slugs');

INSERT INTO ADMIN_PEST_ACTIONS VALUES
(1, 'Aphids'),
(1, 'Caterpillars'),
(1, 'Spider Mites');

INSERT INTO ADMIN_PLANT_ACTIONS VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5);

-- Sample queries
SELECT P.Plant_ID, P.Common_Name, S.Soil_Type, S.pH
FROM PLANT P
JOIN SOIL S ON P.Soil_Type = S.Soil_Type;

UPDATE TASK
SET isCompleted = TRUE
WHERE Task_ID = 1 AND Plant_ID = 1;

DELETE FROM HARVEST_LOG
WHERE Plant_ID = 3 AND ID = 1;
