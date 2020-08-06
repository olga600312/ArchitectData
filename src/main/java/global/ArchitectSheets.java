package global;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.gson.Gson;
import domain.Cause;
import domain.HorizontalLocation;
import domain.LocationType;
import domain.VerticalLocation;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ArchitectSheets {
    private static Sheets sheetsService;
    private static final String APPLICATION_NAME = "Google Sheets Architect API";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String SPREAD_SHEET_ID = "1s1xfOmyuQo2AAb6aC-Idxg-WG5n9_x65gNZI4jDF2H0";//"1QVdmWszVAv7QuUe-Uv7S2Kj5L8QFCsH37z2dtV6pBTo";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    private static Credential authorise(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = ArchitectSheets.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()/*.setPort(8888)*/.build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */
    public static void main(String... args) throws IOException, GeneralSecurityException {
        Collection<VerticalLocation> arr = fetchVerticalLocations();
        System.err.println(new Gson().toJson(arr));
        Collection<Cause> causes = getCauses(arr);
        System.err.println(new Gson().toJson(causes));
        // Build a new authorized API client service.
       /* final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String range = "Sheet1!A1:E5";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorise(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(SPREAD_SHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {

            for (List row : values) {
                // Print columns A and E, which correspond to indices 0 and 4.
                System.out.printf("%s, %s,%s, %s\n", row.get(0), row.get(1),row.get(2), row.get(3));
            }
        }*/
    }

    public static Collection<Cause> getCauses(Collection<VerticalLocation> locations) throws IOException, GeneralSecurityException {
        ArrayList<Cause> arr = new ArrayList<>();
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        String range = "Sheet3!A1:AV29";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorise(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(SPREAD_SHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            ArrayList<String> verticalNames = new ArrayList<>();
            for (List row : values) {
                if (verticalNames.size() == 0) {

                    row.stream().filter(e -> e != null && !e.toString().trim().isEmpty()).forEach(e->verticalNames.add((String) e));
                } else {
                    HorizontalLocation rowHeader = HorizontalLocation.builder().name((String) row.get(0)).build();
                    ArrayList<Cause.Entry> data = new ArrayList<>();
                    Cause c = Cause.builder().data(data).rowHeader(rowHeader).build();

                    ArrayList rowData=new ArrayList();
                    row.stream().skip(1).forEach(rowData::add);
                    for(int i=0;i<rowData.size();i++){
                        Cause.Entry entry = Cause.Entry.builder().total(Integer.parseInt(rowData.get(i).toString())).build();
                        final int index=i;
                        entry.setColumnHeader(locations.stream().filter(e->e.getName().equals(verticalNames.get(index))).findFirst().orElse(null));
                        data.add(entry);
                    };
                    arr.add(c);
                }
            }
        }
        return arr;
    }

    private static Collection<VerticalLocation> fetchVerticalLocations() throws GeneralSecurityException, IOException {
        ArrayList<VerticalLocation> arr = new ArrayList<>();
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        String range = "LocationType!A1:S6";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorise(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(SPREAD_SHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {

            for (List row : values) {
                LocationType type = LocationType.valueOf((String) row.get(0));
               // type.setInitColor(new Color(Integer.parseInt(row.get(1).toString(),16)));
                row.stream().skip(2).filter(e -> e != null && !e.toString().trim().isEmpty()).forEach(e -> {
                    arr.add(VerticalLocation.builder().type(type).name(e.toString()).build());

                });
            }
        }
        return arr;
    }
}
