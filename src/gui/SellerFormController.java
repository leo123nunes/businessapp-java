package gui;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.SellerService;

public class SellerFormController implements Initializable {
	
	private Seller entity;
	
	private SellerService service;
	
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();
	@FXML
	private TextField txtId;
	
	@FXML
	private TextField txtEmail;
	
	@FXML
	private DatePicker txtBirthDate;
	
	@FXML
	private TextField baseSalary;
	
	@FXML
	private TextField txtName;
	
	@FXML
	private Label labelNameError;
	
	@FXML
	private Label labelEmailError;
	
	@FXML
	private Label labelBirthDateError;
	
	@FXML
	private Label labelBaseSalaryError;
	
	@FXML
	private Button btSave;
	
	@FXML
	private Button btCancel;
	
	public void subscribeDataChangeList(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}
	
	public void setSeller(Seller obj) {
		this.entity = obj;
	}
	
	public void setSellerService(SellerService service) {
		this.service = service;
	}
	
	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if(entity == null) {
			throw new IllegalStateException("Error, entity was null.");
		}
		
		if(service == null) {
			throw new IllegalStateException("Error, service was null.");
		}
		try {
			entity = getFormData();
			service.saveOrUpdate(entity);
			notifyDataChangeListeners();
			Utils.currentStage(event).close();
		}catch(ValidationException e) {
			setErrorMessages(e.getErrors());
		}catch(DbException e) {
			Alerts.showAlert("Error saving Seller.", null, e.getMessage(), AlertType.ERROR);
		}
	}
	
	private void notifyDataChangeListeners() {
		for(DataChangeListener listeners : dataChangeListeners) {
			listeners.onDataChanged();
		}
	}

	private Seller getFormData() {
		Seller obj = new Seller();
		
		ValidationException exception = new ValidationException("Validation error");
		
		obj.setId(Utils.tryParseToInt(txtId.getText()));
		
		if(txtName.getText()==null || txtName.getText().trim().equals("")) {
			exception.addError("name", "Name can't be empty.");
		}
		
		obj.setName(txtName.getText());
		
		if(exception.getErrors().size()>0) {
			throw exception;
		}
		
		return obj;
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}
	
	public void initializeNodes() {
		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtName, 20);
		Constraints.setTextFieldDouble(baseSalary);
		Constraints.setTextFieldMaxLength(txtEmail, 60);
		Utils.formatDatePicker(txtBirthDate, "dd/MM/yyyy");
	}
	
	public void updateFormData() {
		if(entity==null) {
			throw new IllegalStateException("Entity was null.");
		}
		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(String.valueOf(entity.getName()));
		txtEmail.setText(entity.getEmail());
		Locale.setDefault(Locale.US);
		baseSalary.setText(String.format("%.2f",entity.getBaseSalary()));
		if(entity.getBirthDate()!=null) {
		txtBirthDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}
	}
	
	private void setErrorMessages(Map<String,String> errors) {
		Set<String> fields = errors.keySet();
		
		if(fields.contains("name")) {
			labelNameError.setText(errors.get("name"));
		}
	}
}
