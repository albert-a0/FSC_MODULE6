package com.example.csc325_firebase_webview_auth.view;//package modelview;

import com.example.csc325_firebase_webview_auth.model.Person;
import com.example.csc325_firebase_webview_auth.viewmodel.AccessDataViewModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class AccessFBView {


     @FXML
    private TextField nameField;
    @FXML
    private TextField majorField;
    @FXML
    private TextField ageField;
    @FXML
    private Button writeButton;
    @FXML
    private Button readButton;
    @FXML
    private TextArea outputField;
    @FXML
    private TableView outputTable;
    @FXML
    private MenuItem menuDeleteTab;

    private ArrayList<String> docIds = new ArrayList<String>();;

     private boolean key;
    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    private Person person;
    public ObservableList<Person> getListOfUsers() {
        return listOfUsers;
    }

    @FXML
    void initialize() {
        AccessDataViewModel accessDataViewModel = new AccessDataViewModel();
        //nameField.textProperty().bindBidirectional(accessDataViewModel.userNameProperty());
        //majorField.textProperty().bindBidirectional(accessDataViewModel.userMajorProperty());
        //writeButton.disableProperty().bind(accessDataViewModel.isWritePossibleProperty().not());


    }

    @FXML
    private void btnClearClicked(ActionEvent event) {

    }

    @FXML
    private void btnAddClicked(ActionEvent event) {

    }

    @FXML
    private void btnEditClicked(ActionEvent event) {

    }

    @FXML
    public void btnDeleteClicked(ActionEvent event) {

    }

    @FXML
    private void addRecord(ActionEvent event) {
        addData();
    }

        @FXML
    private void readRecord(ActionEvent event) {
        readFirebase();
    }

            @FXML
    private void regRecord(ActionEvent event) {
        registerUser();
    }

    @FXML
    private void menuRegisterTabClicked(ActionEvent event) {
        registerUser();
    }

    @FXML
    private void menuCloseTabClicked(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void menuDeleteTabClicked(ActionEvent event) throws ExecutionException, InterruptedException {
        TextInputDialog dialog = new TextInputDialog("<USER ID HERE>");
        dialog.setTitle("REMOVING USER");
        dialog.setHeaderText("This can not be undone");
        dialog.setContentText("Enter the user id of the user:");
        Optional<String> result = dialog.showAndWait();

        App.fstore.collection("References").document(result.get().toString())
                .delete()
                .get();

    }

     @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("/files/WebContainer.fxml");
    }

    public void addData() {

        DocumentReference docRef = App.fstore.collection("References").document(UUID.randomUUID().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("Name", nameField.getText());
        data.put("Major", majorField.getText());
        data.put("Age", Integer.parseInt(ageField.getText()));
        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("ADDED TO THE DATABASE\nPlease click read to view data");

        alert.showAndWait();
    }

        public boolean readFirebase()
         {
             key = false;

             outputTable.getItems().clear();
             outputTable.getColumns().clear();
             outputTable.refresh();

        //asynchronously retrieve all documents
        ApiFuture<QuerySnapshot> future =  App.fstore.collection("References").get();
        // future.get() blocks on response
        List<QueryDocumentSnapshot> documents;
        try
        {
            TableColumn<Person,String> fcol = new TableColumn<>("USER ID");
            fcol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<Person,String> scol = new TableColumn<>("NAME");
            scol.setCellValueFactory(new PropertyValueFactory<>("name"));

            TableColumn<Person,String> tcol = new TableColumn<>("MAJOR");
            tcol.setCellValueFactory(new PropertyValueFactory<>("major"));

            TableColumn<Person,String> lcol = new TableColumn<>("AGE");
            lcol.setCellValueFactory(new PropertyValueFactory<>("age"));

            outputTable.getColumns().add(fcol);
            outputTable.getColumns().add(scol);
            outputTable.getColumns().add(tcol);
            outputTable.getColumns().add(lcol);

            documents = future.get().getDocuments();
            if(documents.size()>0)
            {
                System.out.println("Outing....");
                for (QueryDocumentSnapshot document : documents)
                {
                    String name = document.getData().get("Name").toString();
                    String major =document.getData().get("Major").toString();
                    int age = Integer.parseInt(document.getData().get("Age").toString());
                    person  = new Person(document.getId(),name,major,age);
                    outputTable.getItems().add(person);
                    docIds.add(document.getId());
                }

            }
            else
            {
               System.out.println("No data");
            }
            key=true;

        }
        catch (InterruptedException | ExecutionException ex)
        {
             ex.printStackTrace();
        }
        return key;
    }

        public void sendVerificationEmail() {
        try {
            UserRecord user = App.fauth.getUser("name");
            //String url = user.getPassword();

        } catch (Exception e) {
        }
    }

    public boolean registerUser() {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail("user@example.com")
                .setEmailVerified(false)
                .setPassword("secretPassword")
                .setPhoneNumber("+11234567890")
                .setDisplayName("John Doe")
                .setDisabled(false);

        UserRecord userRecord;
        try {
            userRecord = App.fauth.createUser(request);
            System.out.println("Successfully created new user: " + userRecord.getUid());
            return true;

        } catch (FirebaseAuthException ex) {
           // Logger.getLogger(FirestoreContext.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }
}
