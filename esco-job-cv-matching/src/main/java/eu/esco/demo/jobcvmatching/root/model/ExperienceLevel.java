package eu.esco.demo.jobcvmatching.root.model;

import com.tenforce.exception.TenforceException;

public enum ExperienceLevel {
  executive, director, midSenior, associate, entryLevel, internship, notApplicable, notFound;

  public static ExperienceLevel fromHtml(String htmlValue) {
    if (htmlValue.equals("Executive")) return executive;
    if (htmlValue.equals("Director")) return director;
    if (htmlValue.equals("Mid-Senior level")) return midSenior;
    if (htmlValue.equals("Associate")) return associate;
    if (htmlValue.equals("Entry level")) return entryLevel;
    if (htmlValue.equals("Internship")) return internship;
    if (htmlValue.equals("Not Applicable")) return notApplicable;
    throw new TenforceException("ExperienceLevel not found for html value '{}'", htmlValue);
  }
}
