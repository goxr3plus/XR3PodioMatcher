package main.java.com.goxr3plus.xr3podiomatcher.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.podio.APIFactory;
import com.podio.ResourceFactory;
import com.podio.contact.Profile;
import com.podio.oauth.OAuthClientCredentials;
import com.podio.oauth.OAuthUsernameCredentials;
import com.podio.org.OrgAPI;
import com.podio.search.SearchInAppResponse;
import com.podio.user.UserAPI;
import com.sun.jersey.api.client.WebResource;

public class Main {
	
	private APIFactory apiFactory;
	private ResourceFactory resourceFactory;
	private int IGET3;
	private final String appName = " add here your application name";
	private final String clientSecret = "add here your client secret";
	WebResource webResource;
	
	public Main() {
		
		try {
			
			resourceFactory = new ResourceFactory(new OAuthClientCredentials(appName, clientSecret), new OAuthUsernameCredentials("add here your podio email", "add here your podio password"));
			APIFactory apiFactory = new APIFactory(resourceFactory);
			UserAPI userAPI = apiFactory.getAPI(UserAPI.class);
			Profile profile = userAPI.getProfile();
			System.out.println(profile.getName());
			
			//			String searchWord = "Swi";
			//			
			//			//Search inside IGET3
			IGET3 = apiFactory.getAPI(OrgAPI.class).getOrganizations().get(0).getSpaces().get(0).getId();
			//			webResource = webResource.queryParam("query", searchWord);
			//			webResource = webResource.queryParam("counts", "1");
			
			//Print the Web Resource URL
			//System.out.println("Web Resource URL :" + webResource.toString());
			
			//ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);	
			//SearchInAppResponse results = (SearchInAppResponse) webResource.get(SearchInAppResponse.class).getResults();
			
			//System.out.println("Results matching  : " + webResource.get(SearchInAppResponse.class).getResults().size());
			
			//Get all startups
			getDataFromAngelList();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void getDataFromAngelList() {
		try {
			
			int[] counter = { 0 };
			int[] found = { 0 };
			Document doc = Jsoup.parse(new File("Athens Startups - AngelList.html"), "UTF-8");
			
			//For Excel Document
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("sheet1");// creating a blank sheet
			int[] rownum = { 0 };
			
			//Create a List of elements
			List<Element> list = doc.getElementsByClass("startup-link").stream().filter(e -> !e.text().isEmpty() && !"e-food.gr".equals(e.text())).collect(Collectors.toList());
			System.out.println("Total StartUps: " + list.size());
			
			//Create the Headers of Excel
			Row row0 = sheet.createRow(0);
			row0.createCell(0).setCellValue("Name");
			row0.createCell(1).setCellValue("WebsiteURL");
			row0.createCell(2).setCellValue("Employees");
			row0.createCell(3).setCellValue("Twitter");
			row0.createCell(4).setCellValue("Facebook");
			row0.createCell(5).setCellValue("LinkedIn");
			
			//For each element
			list.forEach(e -> {
				System.out.println("--------------" + (++counter[0] ) + "--------------");
				//if (counter[0] <= 214 || counter[0] >= 240)
				//	return;
				
				String name = e.text().trim();
				boolean exists = checkIfAlreadyExists(name);
				
				//Check if exists
				if (!exists) {
					++found[0];
					System.out.println(rownum[0]++ + ". Name : [ " + name + " ] \n\t->" + exists);
					
					//Create Row
					Row row = sheet.createRow(rownum[0]);
					
					//Create Cell ( Name )
					row.createCell(0).setCellValue(name);
					
					//Site Link
					Document doc2;
					try {
						String href = e.attr("href");
						doc2 = Jsoup.connect(href).get();
						
						//Create Cell ( Link )
						String websiteURL = doc2.getElementsByClass("u-uncoloredLink company_url").attr("href");
						System.out.println(websiteURL);
						row.createCell(1).setCellValue(websiteURL);
						
						//Create Employees
						String employeesNumber = doc2.getElementsByClass("js-company_size").text();
						System.out.println("Employees : " + employeesNumber);
						row.createCell(2).setCellValue(employeesNumber);
						
						//--TWITTER---
						String twitterURL = doc2.getElementsByClass("fontello-twitter u-uncoloredLink twitter_url").attr("href");
						System.out.println("Twitter : " + twitterURL);
						if (!twitterURL.isEmpty() || twitterURL != null)
							row.createCell(3).setCellValue(twitterURL);
						
						//--FACEBOOK---
						String facebookURL = doc2.getElementsByClass("fontello-facebook u-uncoloredLink facebook_url").attr("href");
						System.out.println("Facebook : " + facebookURL);
						if (!facebookURL.isEmpty() || facebookURL != null)
							row.createCell(4).setCellValue(facebookURL);
						
						//--LINKEDIN---
						String linkedInURL = doc2.getElementsByClass("fontello-linkedin u-uncoloredLink linkedin_url").attr("href");
						System.out.println("LinkedIN : " + linkedInURL);
						if (!linkedInURL.isEmpty() || linkedInURL != null)
							row.createCell(5).setCellValue(linkedInURL);
						
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
				}
			});
			
			FileOutputStream out = new FileOutputStream(new File("StartUps.xlsx")); // file name with path
			workbook.write(out);
			out.close();
			
			System.out.println("Finished created excel file");
		} catch (
		
		Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Check in the podio if the item already exists
	 * 
	 * @param searchWord
	 * @return
	 */
	public boolean checkIfAlreadyExists(String searchWord) {
		webResource = resourceFactory.getApiResource("/search/space/" + IGET3 + "/v2");
		webResource = webResource.queryParam("query", searchWord);
		webResource = webResource.queryParam("counts", "1");
		
		return webResource.get(SearchInAppResponse.class).getResults().size() != 0;
	}
	
	public static void main(String[] args) {
		new Main();
	}
	
}
