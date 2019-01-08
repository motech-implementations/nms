use motech_data_services;
SET FOREIGN_KEY_CHECKS=0;
ALTER TABLE `nms_districts` CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT ;
ALTER TABLE `nms_talukas` CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT ;
ALTER TABLE `nms_health_blocks` CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `nms_health_facilities` CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `nms_health_sub_facilities` CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `nms_villages` CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `nms_mother_rejects` CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `nms_child_rejects` CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `nms_imi_cdrs` CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT ;
ALTER TABLE `nms_imi_csrs` CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT ;
<<<<<<< HEAD:Database Instructions.txt

ALTER TABLE `motech_data_services`.`nms_taluka_healthblock`
CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `motech_data_services`.`nms_village_healthsubfacility`
CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT;


=======
ALTER TABLE `motech_data_services`.`nms_taluka_healthblock` CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `motech_data_services`.`nms_village_healthsubfacility` CHANGE COLUMN `id` `id` BIGINT(20) NOT NULL AUTO_INCREMENT;
>>>>>>> ca0ec47354b5a8cf38862d1291aa913fb5858350:AutoIncrementIDsScript.sql
SET FOREIGN_KEY_CHECKS=1;

