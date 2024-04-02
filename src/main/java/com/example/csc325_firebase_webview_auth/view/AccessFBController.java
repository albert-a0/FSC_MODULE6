package com.example.csc325_firebase_webview_auth.view;

import com.example.csc325_firebase_webview_auth.model.Person;
import com.google.cloud.storage.*;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AccessFBController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnadd;

    @FXML
    private Button btnclear;

    @FXML
    private Button btndel;

    @FXML
    private Button btnedit;

    @FXML
    private TextField inpmaj;

    @FXML
    private TextField inpfn;

    @FXML
    private TextField inpid;

    @FXML
    private TextField inpln;

    @FXML
    private TextField inpage;

    @FXML
    private TableView outputTable;

    @FXML
    private MenuItem menuCloseTab;

    // ==========================================================
    private ArrayList<String> docIds = new ArrayList<String>();;

    private boolean key;
    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    private Person person;


    @FXML
    void initialize() {
        assert btnadd != null : "fx:id=\"btnadd\" was not injected: check your FXML file 'AccessFBView.fxml'.";
        assert btnclear != null : "fx:id=\"btnclear\" was not injected: check your FXML file 'AccessFBView.fxml'.";
        assert btndel != null : "fx:id=\"btndel\" was not injected: check your FXML file 'AccessFBView.fxml'.";
        assert btnedit != null : "fx:id=\"btnedit\" was not injected: check your FXML file 'AccessFBView.fxml'.";
        assert inpfn != null : "fx:id=\"inpfn\" was not injected: check your FXML file 'AccessFBView.fxml'.";
        assert inpid != null : "fx:id=\"inpid\" was not injected: check your FXML file 'AccessFBView.fxml'.";
        assert inpln != null : "fx:id=\"inpln\" was not injected: check your FXML file 'AccessFBView.fxml'.";
        assert inpmaj != null : "fx:id=\"inpmaj\" was not injected: check your FXML file 'AccessFBView.fxml'.";
        assert inpage != null : "fx:id=\"inpage\" was not injected: check your FXML file 'AccessFBView.fxml'.";
        assert outputTable != null : "fx:id=\"outputTable\" was not injected: check your FXML file 'AccessFBView.fxml'.";

        inpid.setEditable(false);
        readData();
    }

    @FXML
    void btnAddClicked(ActionEvent event) {
        addData();
    }

    @FXML
    void btnClearClicked(ActionEvent event) {
        clearFields();
    }

    @FXML
    void btnDeleteClicked(ActionEvent event) {
        DocumentReference docRef = App.fstore.collection("References").document(inpid.getText());
        docRef.delete();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("INFORMATION");
        alert.setHeaderText(null);
        alert.setContentText("RECORD HAS BEEN DELETED!");
        alert.showAndWait();
        readData();
    }

    @FXML
    void btnEditClicked(ActionEvent event) throws ExecutionException, InterruptedException {
        DocumentReference docRef = App.fstore.collection("References").document(inpid.getText());
        ApiFuture<WriteResult> future = docRef.update("Name", inpfn.getText() + " " + inpln.getText(),
                "Age",inpage.getText(),"Major",inpmaj.getText());
        WriteResult result = future.get();
        readData();
    }

    @FXML
    void outputTablePressed(KeyEvent event) {
        if(event.getCode() == KeyCode.C) {
            try {
                Person person = (Person) outputTable.getSelectionModel().getSelectedItem();
                String firstName = person.getName().split(" ")[0];
                String lastName = person.getName().split(" ")[1];
                inpid.setText(person.getId());
                inpfn.setText(firstName);
                inpln.setText(lastName);
                inpmaj.setText(person.getMajor());
                inpage.setText(person.getAge() + "");
            }catch (Exception e){
                System.out.println("EMPTY SPACE");
            }


        }
    }

    @FXML
    void menuCloseTabClicked (ActionEvent event) {
        Platform.exit();
    }


    // METHODS ===================
    public void clearFields() {
        inpid.setText("");
        inpfn.setText("");
        inpln.setText("");
        inpmaj.setText("");
        inpage.setText("");
    }

    public void addData() {
        DocumentReference docRef = App.fstore.collection("References").document(UUID.randomUUID().toString());

        try {
            Map<String, Object> data = new HashMap<>();
            String name = inpfn.getText().toString() + " " + inpln.getText().toString();
            String maj = inpmaj.getText().toString();
            int age = Integer.parseInt(inpage.getText().toString());
            data.put("Name", name);
            data.put("Major", maj);
            data.put("Age", age);
            //asynchronously write data
            ApiFuture<WriteResult> result = docRef.set(data);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("ADDED TO THE DATABASE\nPlease click read to view data");
            alert.showAndWait();
            readData();
        }catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR DETECTED");
            alert.setHeaderText(null);
            alert.setContentText("Something happened!\n"+e.getMessage());
            alert.showAndWait();
        }

    };

    public boolean readData() {
        outputTable.getItems().clear();
        outputTable.getColumns().clear();
        outputTable.refresh();

        key = false;
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

}
