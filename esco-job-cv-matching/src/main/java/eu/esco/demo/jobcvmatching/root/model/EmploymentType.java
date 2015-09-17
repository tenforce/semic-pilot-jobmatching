package eu.esco.demo.jobcvmatching.root.model;

import com.tenforce.exception.TenforceException;

public enum EmploymentType {
  fulltime, parttime, contract, temporary, other, notFound;

  public static EmploymentType fromHtml(String htmlValue) {
    if (htmlValue.equals("Full-time")) return fulltime;
    if (htmlValue.equals("Part-time")) return parttime;
    if (htmlValue.equals("Contract")) return contract;
    if (htmlValue.equals("Temporary")) return temporary;
    if (htmlValue.equals("Other")) return other;
    throw new TenforceException("EmploymentType not found for html value '{}'", htmlValue);
  }
}
