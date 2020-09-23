-- Database schema for COSC1295 Assignment

CREATE TABLE `addresses` (
    `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
    `building` VARCHAR(50) DEFAULT NULL,
    `street` VARCHAR(50) NOT NULL,
    `suburb` VARCHAR(50) NOT NULL,
    `state` VARCHAR(30) NOT NULL,
    `post_code` VARCHAR(5) NOT NULL,
    `country` VARCHAR(50) DEFAULT NULL,
    CONSTRAINT `addresses_pk` PRIMARY KEY (`id`)
);

CREATE TABLE `companies` (
    `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
    `address_id` SMALLINT(6) NOT NULL,
    `unique_id` VARCHAR(10) NOT NULL,
    `company_name` VARCHAR(50) NOT NULL,
    `abn_number` VARCHAR(50) DEFAULT NULL,
    `website_url` VARCHAR(50) DEFAULT NULL,
    CONSTRAINT `companies_pk` PRIMARY KEY (`id`),
    CONSTRAINT `companies_addresses_fk` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`id`)
);

CREATE TABLE `rankings` (
    `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
    `subject_id` SMALLINT(6) NOT NULL,
    `subject_type` VARCHAR(10) NOT NULL DEFAULT 'STUDENT',
    CONSTRAINT `rankings_pk` PRIMARY KEY (`id`)
);

CREATE TABLE `skill_rankings` (
    `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
    `ranking_id` SMALLINT(6) NOT NULL,
    `skill` VARCHAR(5) NOT NULL,
    `ranking` TINYINT(3) NOT NULL,
    CONSTRAINT `skill_rankings_pk` PRIMARY KEY (`id`),
    CONSTRAINT `skill_rankings_rankings_fk` FOREIGN KEY (`ranking_id`) REFERENCES `rankings` (`id`) ON DELETE CASCADE
);

CREATE TABLE `roles` (
     `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
     `role` VARCHAR(30) NOT NULL,
     CONSTRAINT `roles_pk` PRIMARY KEY (`id`)
);

CREATE TABLE `project_owners` (
    `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
    `role_id` SMALLINT(6) NOT NULL,
    `company_id` SMALLINT(6) NOT NULL,
    `unique_id` VARCHAR(10) NOT NULL,
    `first_name` VARCHAR(50) NOT NULL,
    `last_name` VARCHAR(50) NOT NULL,
    `email_address` VARCHAR(50) DEFAULT NULL,
    CONSTRAINT `project_owners_pk` PRIMARY KEY (`id`),
    CONSTRAINT `project_owners_roles_fk` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
    CONSTRAINT `project_owners_companies_fk` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
);

CREATE TABLE `projects` (
    `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
    `project_owner_id` SMALLINT(6) NOT NULL,
    `unique_id` VARCHAR(10) NOT NULL,
    `project_title` VARCHAR(50) NOT NULL,
    `brief_description` VARCHAR(50) DEFAULT NULL,
    CONSTRAINT `projects_pk` PRIMARY KEY (`id`)
);

CREATE TABLE `students` (
    `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
    `unique_id` VARCHAR(10) NOT NULL,
    `personality` VARCHAR(5) NOT NULL,
    `conflicter1_id` SMALLINT(6) DEFAULT NULL,
    `conflicter2_id` SMALLINT(6) DEFAULT NULL,
    CONSTRAINT `students_pk` PRIMARY KEY (`id`),
    CONSTRAINT `students_students_fk1` FOREIGN KEY (`conflicter1_id`) REFERENCES `students` (`id`),
    CONSTRAINT `students_students_fk2` FOREIGN KEY (`conflicter2_id`) REFERENCES `students` (`id`)
);

CREATE TABLE `student_preferences` (
    `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
    `student_id` SMALLINT(6) NOT NULL,
    `inserted_on` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `student_preferences_pk` PRIMARY KEY (`id`),
    CONSTRAINT `student_preferences_students_fk` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE CASCADE
);

CREATE TABLE `preferences` (
    `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
    `student_preference_id` SMALLINT(6) NOT NULL,
    `project_id` SMALLINT(6) NOT NULL,
    `rating` TINYINT(6) NOT NULL,
    CONSTRAINT `preferences_pk` PRIMARY KEY (`id`),
    CONSTRAINT `preferences_student_preferences_fk` FOREIGN KEY (`student_preference_id`) REFERENCES `student_preferences` (`id`) ON DELETE CASCADE,
    CONSTRAINT `preferences_projects_fk` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE
);

CREATE TABLE `teams` (
    `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
    `project_id` SMALLINT(6) NOT NULL,
    CONSTRAINT `teams_pk` PRIMARY KEY (`id`),
    CONSTRAINT `teams_projects_fk` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`)
);

CREATE TABLE `team_members` (
    `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
    `team_id` SMALLINT(6) NOT NULL,
    `student_id` SMALLINT(6) NOT NULL,
    CONSTRAINT `team_members_pk` PRIMARY KEY (`id`),
    CONSTRAINT `team_members_teams_fk` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE,
    CONSTRAINT `team_members_students_fk` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE CASCADE
);

CREATE TABLE `fitness_metrics` (
    `id` SMALLINT(6) NOT NULL AUTO_INCREMENT,
    `team_id` SMALLINT(6) NOT NULL,
    `avg_skill_competency` FLOAT(5,2) NOT NULL,
    `competency_by_skill` NVARCHAR(100) NOT NULL, -- Eg: P,,1.65,,W,,0.45,,A,,2.0,,N,,1.25,,
    `avg_preference_satisfaction` NVARCHAR(100) NOT NULL, -- Eg: 51.1,,34.5,,67.7,,
    `avg_skill_shortfall` FLOAT(5,2) NOT NULL,
    `shortfall_by_project` VARCHAR(250) NOT NULL, -- Eg: PRO2,,1.65,,PRO1,,0.45,,PRO4,,2.0,,PRO3,,1.25,,
    CONSTRAINT `fitness_metrics_pk` PRIMARY KEY (`id`),
    CONSTRAINT `fitness_metrics_teams_fk` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE
);

CREATE TRIGGER `students_after_delete_trigger`
    AFTER DELETE ON `students` FOR EACH ROW
BEGIN
    DELETE FROM `rankings` WHERE `subject_id` = OLD.id AND `subject_type` = 'STUDENT';
END;


CREATE TRIGGER `projects_after_delete_trigger`
    AFTER DELETE ON `projects` FOR EACH ROW
BEGIN
    DELETE FROM `rankings` WHERE `subject_id` = OLD.id AND `subject_type` = 'PROJECT';
END;


-- ----------------------------------------------------------------------------------------------------------------
-- Queries to get data for the app

SELECT `student_unique_id` AS student_id, `project_unique_id` AS `project_id`, `rating`
  FROM (
    SELECT P.`student_preference_id` AS preference_id, S.`id` AS `student_id`, S.`unique_id` AS student_unique_id, PR.`unique_id` AS project_unique_id, P.`rating`
    FROM `students` S, `preferences` P, `projects` PR
    WHERE P.`project_id` = PR.`id`) Q1 JOIN (
       SELECT SP.`id`, sp.`student_id`, MAX(SP.`inserted_on`) AS inserted_on FROM `student_preferences` SP GROUP BY SP.`student_id`
    ) Q2 ON Q1.`preference_id` = Q2.`id` AND Q1.`student_id` = Q2.`student_id` ORDER BY student_id;


SELECT P.*, SR.`skill`, SR.`ranking` FROM `projects` P, `rankings` R, `skill_rankings` SR
WHERE R.`subject_id` = P.`id` AND SR.`ranking_id` = R.`id` AND R.`subject_type` = 'PROJECT';


SELECT S1.`id`, S1.`unique_id`, S1.`personality`, S2.`unique_id` AS first_conflicter, S3.`unique_id` AS second_conflicter
  FROM `students` S1 LEFT JOIN `students` S2 ON S1.`conflicter1_id` = S2.`id`
  LEFT JOIN `students` S3 ON S1.`conflicter2_id` = S3.`id`;


SELECT T1.*, F.`id` AS `fitness_metric_id`
  FROM (
    SELECT T.*, S.`unique_id` FROM `teams` T, `team_members` M, `students` S
    WHERE T.`id` = M.`team_id` AND M.`student_id` = S.`id`
  ) T1 LEFT JOIN `fitness_metrics` F ON F.`team_id` = T1.`id`;

SELECT S.*, SR.`skill`, SR.`ranking` FROM (
   SELECT S1.`id`, S1.`unique_id`, S1.`personality`, S2.`unique_id` AS first_conflicter, S3.`unique_id` AS second_conflicter
   FROM `students` S1 LEFT JOIN `students` S2 ON S1.`conflicter1_id` = S2.`id`
                      LEFT JOIN `students` S3 ON S1.`conflicter2_id` = S3.`id`) S, `skill_rankings` SR, `rankings` R
WHERE S.`id` = R.`subject_id` AND R.`id` = SR.`ranking_id` AND R.`subject_type` = 'STUDENT';