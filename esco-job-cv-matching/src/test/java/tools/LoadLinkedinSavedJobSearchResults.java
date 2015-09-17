package tools;

import com.tenforce.core.io.FileExtensionFilter;
import com.tenforce.core.io.FileHelper;
import com.tenforce.core.net.HttpClientFactory;
import com.tenforce.core.spring.ResourceHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.StatusLine;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class LoadLinkedinSavedJobSearchResults {

  public static void main(String[] args) throws Exception {
    LoadLinkedinSavedJobSearchResults loadLinkedinSavedJobSearchResults = new LoadLinkedinSavedJobSearchResults();

    File destinationFolder = new File("C:\\Temp\\esco\\semic\\jobSearch\\jobs_20150309_01");
    destinationFolder.mkdirs();

    File[] htmlFiles = new File("C:\\Temp\\esco\\semic\\jobSearch\\linkedin_20150309_01").listFiles(new FileExtensionFilter("html"));
    for (File htmlFile : htmlFiles) {
      loadLinkedinSavedJobSearchResults.downloadJobsForSearchResults(new FileSystemResource(htmlFile), destinationFolder);
    }
    loadLinkedinSavedJobSearchResults.httpClientFactory.close();
  }

  private final Set<Integer> jobIds = new HashSet<>();
  private final HttpClientFactory httpClientFactory = new HttpClientFactory(null);

  public void downloadJobsForSearchResults(Resource resource, File destinationFolder) throws IOException {
    String html = ResourceHelper.toString(resource, "UTF-8");
    int index = 0;

    HttpClientFactory httpClientFactory = new HttpClientFactory(null);
    while ((index = html.indexOf("https://www.linkedin.com/jobs?viewJob=&jobId=", index)) > 0) {
      index += 45;
      int end = html.indexOf("&", index);
      int jobId = Integer.parseInt(html.substring(index, end));
      if (!jobIds.add(jobId)) continue;

      File jobFile = new File(destinationFolder, jobId + ".html");
      if (jobFile.exists()) {
        System.out.println("job file already exists: " + jobFile);
        continue;
      }
      Pair<StatusLine, String> result = httpClientFactory.executeStringGet("https://www.linkedin.com/jobs2/view/" + jobId);
      if (!HttpClientFactory.isSuccess(result.getLeft())) {
        FileHelper.writeStringToFile(jobFile, "Failed: " + result.getLeft());
        System.out.println("Get failed for " + jobId + ": " + result.getLeft());
        continue;
      }
      FileHelper.writeStringToFile(jobFile, result.getRight());
    }
  }
}
