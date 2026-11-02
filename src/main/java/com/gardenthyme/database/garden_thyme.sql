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
-- PEST_PLANT_LINK
-- -----------------------------------------------------
CREATE TABLE PEST_PLANT_LINK (
    Pest_Name VARCHAR(100),
    Plant_ID INT,
    PRIMARY KEY (Pest_Name, Plant_ID),
    FOREIGN KEY (Pest_Name) REFERENCES PESTS(Name),
    FOREIGN KEY (Plant_ID) REFERENCES PLANT(Plant_ID)
);

-- -----------------------------------------------------
-- SAMPLE DATA
-- -----------------------------------------------------
INSERT INTO SOIL VALUES 
('Loam', 6.5),
('Clay', 5.8),
('Sandy', 6.0);

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

INSERT INTO PESTS VALUES
('Aphids', 'Neem Oil, Insecticidal Soap'),
('Caterpillars', 'Bt Spray, Hand picking'),
('Spider Mites', 'Insecticidal Soap, Neem Oil'),
('Whiteflies', 'Yellow sticky traps, Insecticidal soap'),
('Slugs', 'Beer traps, Diatomaceous earth');

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

