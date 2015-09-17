package eu.esco.demo.jobcvmatching.root;

public interface SchemaOrgModel {
  String base = "http://schema.org/";

  String typeJobPosting = base + "JobPosting";
  String propertyJobPostingOccupationalCategory = typeJobPosting + "/occupationalCategory";
  String propertyJobPostingDescription = typeJobPosting + "/description";
  String propertyJobPostingHiringOrganization= typeJobPosting + "/hiringOrganization";
  String propertyJobPostingIndustry= typeJobPosting + "/industry";

  String typeOrganization = base + "Organization";
  String propertyOrganizationName = typeOrganization + "/name";
}
