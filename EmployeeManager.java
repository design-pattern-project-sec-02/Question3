import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.SimpleFloatProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.VBox;

public class EmployeeManager extends Application {

    private HashMap<Integer, EmployeeNode> employeeNodes = new HashMap<>();

    private EmployeeNode rootEmployeeNode;
    private TreeView<Employee> treeView;

    public static void main(String[] args) {
        Application.launch(args);
    }

    private void populateEmployeeNodes() {

        Employee ceo = new Employee("Mr. CEO", "CEO", 10000);
        Employee vpMarketing = new Employee("VP Mkt", "VP", 9000);
        Employee vpProduction = new Employee("VP Prod", "VP", 9000);
        Employee mgrSales = new Employee("Sales Mgr", "Manager", 8000);
        Employee mgrMarketing = new Employee("Marketing Mgr", "Manager", 8000);
        Employee mgrProduction = new Employee("Prod Mgr", "Manager", 8000);
        Employee mgrShipping = new Employee("Shipping Mgr", "Manager", 8000);
        Employee asst1Sales = new Employee("Sales", "Assistant", 5000);
        Employee asst2Sales = new Employee("Sales", "Assistant", 5000);
        Employee asstMarketing = new Employee("Secy", "Assistant", 5000);
        Employee asst1Production = new Employee("Manual", "Assistant", 5000);
        Employee asst2Production = new Employee("Manual", "Assistant", 5000);
        Employee asst3Production = new Employee("Manual", "Assistant", 5000);
        Employee asst1Ship = new Employee("Ship", "Assistant", 5000);
        Employee asst2Ship = new Employee("Ship", "Assistant", 5000);

        EmployeeNode ceoNode = new EmployeeNode(ceo);
        ceoNode.children.addAll(vpMarketing, vpProduction);

        EmployeeNode marketingNode = new EmployeeNode(vpMarketing);
        marketingNode.children.addAll(mgrSales, mgrMarketing);

        EmployeeNode productionNode = new EmployeeNode(vpProduction);
        productionNode.children.addAll(mgrProduction, mgrShipping);

        EmployeeNode mgrSalesNode = new EmployeeNode(mgrSales);
        mgrSalesNode.children = FXCollections.observableArrayList(asst1Sales, asst2Sales);

        EmployeeNode mgrMarketingNode = new EmployeeNode(mgrMarketing);
        mgrMarketingNode.children.addAll(asstMarketing);

        EmployeeNode mgrProductionNode = new EmployeeNode(mgrProduction);
        mgrProductionNode.children.addAll(asst1Production, asst2Production, asst3Production);


        EmployeeNode mgrShippingNode = new EmployeeNode(mgrShipping);
        mgrShippingNode.children.addAll(asst1Ship, asst2Ship);

        employeeNodes.put(ceo.employeeId, ceoNode);
        employeeNodes.put(vpMarketing.employeeId, marketingNode);
        employeeNodes.put(vpProduction.employeeId, productionNode);
        employeeNodes.put(mgrSales.employeeId, mgrSalesNode);
        employeeNodes.put(mgrMarketing.employeeId, mgrMarketingNode);
        employeeNodes.put(mgrProduction.employeeId, mgrProductionNode);
        employeeNodes.put(mgrShipping.employeeId, mgrShippingNode);

        rootEmployeeNode = ceoNode;
    }

    @Override
    public void start(Stage stage) {

        populateEmployeeNodes();

        List<EmployeeNode> nodesToAdd = new ArrayList<>();
        List<EmployeeNode> recruits = new ArrayList<>();
        nodesToAdd.add(rootEmployeeNode);
        HashMap<Integer, TreeItem<Employee>> treeItems = new HashMap<>();

        TreeItem<Employee> rootNode = new TreeItem<>(rootEmployeeNode.self);
        rootNode.setExpanded(true);

        treeItems.put(rootEmployeeNode.self.employeeId, rootNode);

        TreeItem<Employee> savedNode, newNode;
        while(nodesToAdd.size() > 0) {
            recruits.clear();
            for(EmployeeNode node: nodesToAdd) {
                savedNode = treeItems.get(node.self.employeeId);
                treeItems.put(node.self.employeeId, savedNode);
                for(Employee emp: node.children) {
                    newNode = new TreeItem<>(emp);
                    savedNode.getChildren().add(newNode);
                    treeItems.put(emp.employeeId, newNode);
                    if(employeeNodes.get(emp.employeeId) != null) {
                        recruits.add(employeeNodes.get(emp.employeeId));
                    }
                }
            }
            nodesToAdd.clear();
            nodesToAdd.addAll(recruits);
        }


        stage.setTitle("Bukaya Start Up Human Resources");
        VBox box = new VBox();
        final Scene scene = new Scene(box, 400, 300);
        scene.setFill(Color.LIGHTGRAY);

        treeView = new TreeView<>(rootNode);
        treeView.setEditable(true);
        treeView.setCellFactory(p -> new TextFieldTreeCellImpl());

        box.getChildren().add(treeView);
        stage.setScene(scene);
        stage.show();
    }

    private float computeSalary(Employee employee) {
        float salaryTotal = employee.getSalary();
        EmployeeNode topNode = employeeNodes.get(employee.employeeId);
        if(topNode == null) {
            return salaryTotal;
        }
        List<EmployeeNode> nodesToProcess = new ArrayList<>();
        List<EmployeeNode> temporaryNodes = new ArrayList<>();
        EmployeeNode empNode;
        nodesToProcess.add(topNode);
        while(nodesToProcess.size() > 0) {
            temporaryNodes.clear();
            for(EmployeeNode node: nodesToProcess) {
                for(Employee emp: node.children) {
                    salaryTotal += emp.getSalary();
                    empNode = employeeNodes.get(emp.employeeId);
                    if(empNode != null) {
                        temporaryNodes.add(empNode);
                    }
                }
            }
            nodesToProcess.clear();
            nodesToProcess.addAll(temporaryNodes);
        }
        return salaryTotal;
    }

    private final class TextFieldTreeCellImpl extends TreeCell<Employee> {

        private TextField textField;
        private ContextMenu contextMenu = new ContextMenu();

        TextFieldTreeCellImpl() {

            MenuItem addMenuItem = new MenuItem("Add Employee");
            contextMenu.getItems().add(addMenuItem);
            addMenuItem.setOnAction(t -> {
                Employee emp = new Employee("New Employee", "Whatever", 1000);
                EmployeeNode employeeNode = employeeNodes.get(getItem().employeeId);
                if(employeeNode == null) {
                    employeeNode = new EmployeeNode(getItem());
                    employeeNodes.put(getItem().employeeId, employeeNode);
                }
                employeeNodes.get(getItem().employeeId).children.add(emp);
                TreeItem<Employee> newEmployee =
                        new TreeItem<>(emp);
                getTreeItem().getChildren().add(newEmployee);
            });

            MenuItem removeMenuItem = new MenuItem("Remove Employee");
            contextMenu.getItems().add(removeMenuItem);
            removeMenuItem.setOnAction(t -> {
                TreeItem c = treeView.getSelectionModel().getSelectedItem();
                c.getParent().getChildren().remove(c);
                Employee emp = (Employee) c.getValue();
                employeeNodes.remove(emp.employeeId);
            });

            MenuItem salaryMenuItem = new MenuItem("Compute Salary");
            contextMenu.getItems().add(salaryMenuItem);
            salaryMenuItem.setOnAction(t -> {
                TreeItem c = treeView.getSelectionModel().getSelectedItem();
                Employee emp = (Employee) c.getValue();
                float totalSalary = computeSalary(emp);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Salary Information");
                alert.setHeaderText(null);
                alert.setContentText("Total salary under the control span of " + emp.getName() + " : " +
                        totalSalary + " Birr.");
                alert.showAndWait();
            });
        }

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null) {
                createTextField();
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(getItem().getName());
            setGraphic(getTreeItem().getGraphic());
        }

        @Override
        public void updateItem(Employee item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(getTreeItem().getGraphic());
                    setContextMenu(contextMenu);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setOnKeyReleased(t -> {
                if (t.getCode() == KeyCode.ENTER) {
                    getItem().setName(textField.getText());
                    commitEdit(getItem());
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });

        }

        private String getString() {
            return getItem() == null ? "" : getItem().getName();
        }
    }

    public static class Employee {

        private final SimpleStringProperty name;
        private final SimpleStringProperty role;
        private final SimpleFloatProperty salary;
        private int employeeId;

        private static int counter = 0;

        private Employee(String name, String role, float salary) {
            this.name = new SimpleStringProperty(name);
            this.role = new SimpleStringProperty(role);
            this.salary = new SimpleFloatProperty(salary);
            counter++;
            this.employeeId = counter;
        }

        @SuppressWarnings("unused")
        String getName() {
            return name.get();
        }

        @SuppressWarnings("unused")
        void setName(String fName) {
            name.set(fName);
        }

        @SuppressWarnings("unused")
        String getRole() {
            return role.get();
        }

        @SuppressWarnings("unused")
        public void setRole(String fName) {
            role.set(fName);
        }

        float getSalary() {
            return salary.get();
        }

        @SuppressWarnings("unused")
        public void setSalary(float salary) {
            this.salary.set(salary);
        }

        @Override
        public String toString() {
            return this.name.get();
        }
    }

    public static class EmployeeNode {

        EmployeeNode(Employee self) {
            this.self = self;
            this.children = FXCollections.observableArrayList();
        }

        Employee self;
        ObservableList<Employee> children;
    }
}