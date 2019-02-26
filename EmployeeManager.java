import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javafx.scene.layout.VBox;

public class EmployeeManager extends Application {

    private MangerEmployee ceo;
    private TreeView<Employee> treeView;

    public static void main(String[] args) {
        Application.launch(args);
    }

    private void populateEmployeeNodes() {

        ceo = new MangerEmployee("Mr. CEO", 10000);
        MangerEmployee vpMarketing = new MangerEmployee("VP Mkt", 9000);
        MangerEmployee vpProduction = new MangerEmployee("VP Prod", 9000);
        MangerEmployee mgrSales = new MangerEmployee("Sales Mgr", 8000);
        MangerEmployee mgrMarketing = new MangerEmployee("Marketing Mgr", 8000);
        MangerEmployee mgrProduction = new MangerEmployee("Prod Mgr", 8000);
        MangerEmployee mgrShipping = new MangerEmployee("Shipping Mgr", 8000);
        Employee asst1Sales = new OrdinaryEmployee("Sales",  5000);
        Employee asst2Sales = new OrdinaryEmployee("Sales",  5000);
        Employee asstMarketing = new OrdinaryEmployee("Secy",  5000);
        Employee asst1Production = new OrdinaryEmployee("Manual",  5000);
        Employee asst2Production = new OrdinaryEmployee("Manual",  5000);
        Employee asst3Production = new OrdinaryEmployee("Manual",  5000);
        Employee asst1Ship = new OrdinaryEmployee("Ship",  5000);
        Employee asst2Ship = new OrdinaryEmployee("Ship",  5000);

        ceo.addSubordinates(vpMarketing, vpProduction);
        vpMarketing.addSubordinates(mgrSales, mgrMarketing);
        vpProduction.addSubordinates(mgrProduction, mgrShipping);

        mgrSales.addSubordinates(asst1Sales, asst2Sales);
        mgrMarketing.addSubordinates(asstMarketing);
        mgrProduction.addSubordinates(asst1Production, asst2Production, asst3Production);
        mgrShipping.addSubordinates(asst1Ship, asst2Ship);

    }

    @Override
    public void start(Stage stage) {

        populateEmployeeNodes();

        List<MangerEmployee> employeesToAdd = new ArrayList<>();
        List<MangerEmployee> recruits = new ArrayList<>();
        employeesToAdd.add(ceo);
        HashMap<Integer, TreeItem<Employee>> treeItems = new HashMap<>();

        TreeItem<Employee> rootNode = new TreeItem<>(ceo);
        rootNode.setExpanded(true);

        treeItems.put(ceo.getId(), rootNode);
        TreeItem<Employee> parentNode, childNode;
        while (employeesToAdd.size() > 0) {
            recruits.clear();
            for (MangerEmployee manager : employeesToAdd) {
                parentNode = treeItems.get(manager.getId());
                for (Employee emp : manager.getSubordinates()) {
                    childNode = new TreeItem<>(emp);
                    parentNode.getChildren().add(childNode);
                    treeItems.put(emp.getId(), childNode);
                    if (emp.isManager()) {
                        recruits.add((MangerEmployee) emp);
                    }
                }
            }
            employeesToAdd.clear();
            employeesToAdd.addAll(recruits);
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

    private final class TextFieldTreeCellImpl extends TreeCell<Employee> {

        private TextField textField;
        private ContextMenu contextMenu = new ContextMenu();

        TextFieldTreeCellImpl() {

            MenuItem addMenuItem = new MenuItem("Add Employee");
            contextMenu.getItems().add(addMenuItem);
            addMenuItem.setOnAction(t -> {

                if(getItem().isManager()) {
                    MangerEmployee parent = (MangerEmployee)getItem();
                    Employee sibling = parent.getSubordinates().size() > 0 ? parent.getSubordinates().get(0) : null;
                    Employee newEmployee;
                    if(sibling == null || !sibling.isManager()) {
                        float salary = sibling == null ? 5000 : sibling.getSalary();
                        newEmployee = new OrdinaryEmployee("New Employee",  salary);
                    } else  {
                        newEmployee = new MangerEmployee("New Manager",  sibling.getSalary());
                    }
                    parent.addSubordinates(newEmployee);
                    TreeItem<Employee> newNode = new TreeItem<>(newEmployee);
                    getTreeItem().getChildren().add(newNode);
                }

            });

            MenuItem removeMenuItem = new MenuItem("Remove Employee");
            contextMenu.getItems().add(removeMenuItem);
            removeMenuItem.setOnAction(t -> {
                TreeItem c = treeView.getSelectionModel().getSelectedItem();
                c.getParent().getChildren().remove(c);
                Employee emp = (Employee) c.getValue();
                emp.getParent().removeSubordinate(emp);
            });

            MenuItem salaryMenuItem = new MenuItem("Compute Salary");
            contextMenu.getItems().add(salaryMenuItem);
            salaryMenuItem.setOnAction(t -> {
                TreeItem c = treeView.getSelectionModel().getSelectedItem();
                Employee emp = (Employee) c.getValue();
                float totalSalary = emp.getControlSpanCost();
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
    
    static abstract class Employee {

        private String name;
        private float salary;
        private int employeeId;
        private MangerEmployee parent;

        private static int counter = 0;

        private Employee(String name, float salary) {
            this.name = name;
            this.salary = salary;
            counter++;
            this.employeeId = counter;
        }


        @Override
        public String toString() {
            return this.getName();
        }

        String getName() {
            return name;
        }

        int getId() {
            return employeeId;
        }

        void setName(String name) {
            this.name = name;
        }

        float getSalary() {
            return salary;
        }

        abstract float getControlSpanCost();

        abstract boolean isManager();

        MangerEmployee getParent() {
            return parent;
        }

        void setParent(MangerEmployee manager) {
            this.parent = manager;
        }
    }

    static class OrdinaryEmployee extends Employee {

        OrdinaryEmployee(String name, float salary) {
            super(name, salary);
        }

        @Override
        float getControlSpanCost() {
            return this.getSalary();
        }

        @Override
        boolean isManager() {
            return false;
        }

    }

    static class MangerEmployee extends Employee {

        private List<Employee> subordinates;

        MangerEmployee(String name, float salary) {
            super(name, salary);
            subordinates = new ArrayList<>();
        }

        @Override
        float getControlSpanCost() {
            float controlCost = getSalary();
            for (Employee emp : subordinates) {
                controlCost += emp.getControlSpanCost();
            }
            return controlCost;
        }

        @Override
        boolean isManager() {
            return true;
        }

        void addSubordinates(Employee... employees) {
            for(Employee emp: employees) {
                emp.setParent(this);
            }
            Collections.addAll(subordinates, employees);
        }

        List<Employee> getSubordinates() {
            return subordinates;
        }

        void removeSubordinate(Employee emp) {
            subordinates.remove(emp);
        }
    }

}
