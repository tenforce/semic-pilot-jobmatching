package eu.esco.demo.jobcvmatching.root;

public interface SemicRdfModel {
  String namespace = "http://data.europa.eu/esco/semic";
  String skillBaseUri = namespace + "/skill/";
  String occupationBaseUri = namespace + "/occupation/";
  String model = namespace + "/model#";

  String typeCV = model + "CV";
  String typeJV = model + "JV";
  String typeSemicConcept = model + "SemicConcept";

  String sectorHosp = namespace + "/sector#HOSP";

  String propertyInSector = model + "inSector";

  String propertyHasSkill_v0 = model + "hasSkill";
  String propertyHasSkill_v1 = model + "hasSkill_v1";
  String propertyHasSkill_toUseForAdd = propertyHasSkill_v1;

  String propertyHasOccupation_v0 = model + "hasOccupation";
  String propertyHasOccupation_v1 = model + "hasOccupation_v1";
  String propertyHasOccupation_toUseForAdd = propertyHasOccupation_v1;

  String propertyHasIsco = model + "hasIsco";
  String propertyHasNace = model + "hasNace";
  String propertyHiringOrganizationName = model + "hiringOrganizationName";
  String propertyEmploymentType = model + "employmentType";
  String propertyExperienceLevel = model + "experienceLevel";
  String propertyOccupationalCategory = model + "occupationalCategory";
  String propertyLocation = model + "location";
  String propertyLocationCore = model + "locationCore";
  String propertyPostcode = model + "postcode";
  String propertyGeonameUri = model + "geonameUri";
  String propertyGeoname = model + "geoname";

  String escoPropertyFallbackLabel = "http://www.tenforce.com/esco/java#fallbackLabel";


  String coreVocabularyPropertyGeographicName = "http://www.w3.org/ns/locn#geographicName";

  String escoTypeSkill = "http://data.europa.eu/esco/model#Skill";
  String escoTypeOccupation = "http://data.europa.eu/esco/model#Occupation";

}

