package com.ruinscraft.stickers;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.*;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GoogleSheetsStickerCodeStorage implements StickerCodeStorage {

    private static final String APPLICATION_NAME = "ruinscraft-stickers/1.0";
    private static final String SPREADSHEET_ID = "1HLXakont6evP1kDYs7o7QLn9QdamVZ23Qk8eQprtp94";
    private static Sheets sheets;

    private static Credential authorize(File credsFile, File tokenFile) throws IOException, GeneralSecurityException {
        InputStream in = new FileInputStream(credsFile);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JacksonFactory.getDefaultInstance(),
                new InputStreamReader(in)
        );
        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                clientSecrets,
                scopes
        )
                .setDataStoreFactory(new FileDataStoreFactory(tokenFile))
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("ruinscraft-stickers-plugin");

        return credential;
    }

    private static Sheets getSheets(File credsFile, File tokenFile) throws IOException, GeneralSecurityException {
        Credential credential = authorize(credsFile, tokenFile);

        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private StickersPlugin stickersPlugin;

    public GoogleSheetsStickerCodeStorage(StickersPlugin stickersPlugin) {
        this.stickersPlugin = stickersPlugin;
    }

    @Override
    public CompletableFuture<String> queryCode(UUID mojangId) {
        return CompletableFuture.supplyAsync(() -> {
            if (sheets == null) {
                try {
                    sheets = getSheets(new File(stickersPlugin.getDataFolder(), "credentials.json"), new File(stickersPlugin.getDataFolder(), "tokens"));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }

            String range = "Sheet1";

            try {
                ValueRange response = sheets.spreadsheets()
                        .values()
                        .get(SPREADSHEET_ID, range)
                        .execute();

                List<List<Object>> values = response.getValues();

                if (values == null || values.isEmpty()) {
                    return null;
                }

                for (List<Object> row : values) {
                    String uuid = row.get(0).toString();
                    String code = row.get(1).toString();

                    if (mojangId.toString().equals(uuid)) {
                        return code;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    @Override
    public CompletableFuture<Void> insertCode(UUID mojangId, String code) {
        return CompletableFuture.runAsync(() -> {
            if (sheets == null) {
                try {
                    sheets = getSheets(new File(stickersPlugin.getDataFolder(), "credentials.json"), new File(stickersPlugin.getDataFolder(), "tokens"));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }

            ValueRange append = new ValueRange()
                    .setValues(Arrays.asList(
                            Arrays.asList(mojangId.toString(), code, getCurrentTimeStamp())
                    ));

            try {
                sheets.spreadsheets()
                        .values()
                        .append(SPREADSHEET_ID, "Sheet1", append)
                        .setValueInputOption("USER_ENTERED")
                        .setInsertDataOption("INSERT_ROWS")
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

}
