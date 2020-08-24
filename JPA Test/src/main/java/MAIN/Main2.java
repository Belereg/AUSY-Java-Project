package MAIN;

import Entity.Department;
import Entity.Employee;
import Entity.Product;
import com.mysql.cj.log.LoggingProfilerEventHandler;
//import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.persistence.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main2 {
    public static void main(String[] args) throws ParseException {

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("my-persistence-unit");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        Logger logger = Logger.getLogger(Main2.class.getName());
        Audit audit = Audit.getInstance();
        Scanner scan = new Scanner(System.in);
        Scanner switchScanner = new Scanner(System.in);
        Scanner subSwitchScanner = new Scanner(System.in);
        Scanner aux = new Scanner(System.in);

        int menu = 0;

        while (menu != 5) {

            System.out.print("Choose the operation:" +
                    "\n1) Insert a department" +
                    "\n2) Find and/or update a product" +
                    "\n3) Delete an employee" +
                    "\n4) Show employees" +
                    "\n5) Quit application" +
                    "\n");

            menu = scan.nextInt();
            if (menu < 1 || menu > 5)
                logger.warning("Not a VALID option");

            switch (menu) {
                case 1:
                    logger.info("SELECTED: " + menu + ") Insert a department");
                    System.out.print("What department you want to be added: ");
                    String departmentOption = switchScanner.nextLine();
                    if (departmentOption == null || departmentOption.isEmpty()) {
                        logger.warning("The name you've entered is null or blank");
                        break;
                    }
                    Department department = new Department();
                    department.setName(departmentOption);
                    entityTransaction.begin();
                    entityManager.persist(department);
                    entityTransaction.commit();
                    audit.writePersonsToFile(department.toString() + " a fost adaugat. ");
                    break;

                case 2:
                    logger.info("SELECTED: " + menu + ") Find and/or update a product");
                    System.out.print("What is the ID of the product you are searching: ");
                    int id;
                    try {
                        id = switchScanner.nextInt();
                    } catch (InputMismatchException exception) {
                        logger.log(Level.SEVERE, "Not a valid option", exception);
                        throw new InputMismatchException();
                    }

                    Product product = entityManager.find(Product.class, id);
                    System.out.println("Product found: (" + product.getName() + ", " + product.getPrice() + ", " + product.getDate() + ")\n");
                    audit.writePersonsToFile(product.toString() + " was found. ");
                    System.out.print("Do you want to modify anything? : " +
                            "\n1) Name" +
                            "\n2) Price" +
                            "\n3) Expiration Date" +
                            "\n4) I don't want to modify anything" +
                            "\n");

                    int modification = switchScanner.nextInt();
                    if (modification < 1 || modification > 4) {
                        logger.warning("The option you chose to modify is invalid");
                        break;
                    }
                    switch (modification) {
                        case 1:
                            System.out.print("What is the new name: ");
                            String newName = subSwitchScanner.nextLine();
                            if (newName == null || newName.isEmpty()) {
                                logger.warning("The name you've entered is null or blank");
                                break;
                            }
                            product.setName(newName);
                            entityTransaction.begin();
                            entityTransaction.commit();
                            audit.writePersonsToFile(product.toString() + " was modified (name). ");
                            break;

                        case 2:
                            System.out.print("What is the new price: ");
                            double newPrice = subSwitchScanner.nextDouble();
                            if (newPrice < 0) {
                                logger.warning("The price cannot be less than 0");
                                break;
                            }
                            product.setPrice(newPrice);
                            entityTransaction.begin();
                            entityTransaction.commit();
                            audit.writePersonsToFile(product.toString() + " was modified (price). ");
                            break;

                        case 3:
                            System.out.print("What is the new expiration date (yyyy-mm-dd): ");
                            String newDate = subSwitchScanner.nextLine();
                            if (newDate == null || newDate.isEmpty()) {
                                logger.warning("The date you've entered is null or blank");
                                break;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            LocalDate localDate = LocalDate.parse(newDate, formatter);
                            System.out.println("date = " + localDate);
                            product.setDate(localDate);
                            entityTransaction.begin();
                            entityTransaction.commit();
                            audit.writePersonsToFile(product.toString() + " was modified (date). ");
                            break;

                    }
                    break;

                case 3:
                    logger.info("SELECTED: " + menu + ") Delete an employee");
                    System.out.println("What's the ID of the employee you want to delete: ");
                    int employeeId = switchScanner.nextInt();
                    if (employeeId < 0) {
                        logger.warning("The ID cannot be less tha 0");
                        break;
                    }
                    System.out.println("SCANNED = " + employeeId + '\n');
                    Employee employee = entityManager.find(Employee.class, employeeId);
                    entityTransaction.begin();
                    entityManager.remove(employee);
                    entityTransaction.commit();
                    audit.writePersonsToFile(employee.toString() + " was deleted. ");
                    break;

                case 4:
                    logger.info("SELECTED: " + menu + ") Show employees");
                    System.out.println("1) List all employees" +
                            "\n2) List all employees ordered by name" +
                            "\n3) List all employees ordered by salary" +
                            "\n4) List all employees from a specified department (By ID)");

                    int listOption = aux.nextInt();
                    try {
                        if (listOption <= 0 || listOption > 4) {
                            throw new IllegalArgumentException();
                        }
                    } catch (IllegalArgumentException exception) {
                        logger.log(Level.SEVERE, "The option cannot be less than ZERO", exception);
                        break;
                    }
                    Query query;

                    if (listOption == 1 || listOption == 2 || listOption == 3) {
                        query = entityManager.createQuery(setJql(listOption));
                        showEmployees(query);
                        audit.writePersonsToFile("Employees listed.");
                    } else {
                        System.out.println("Department ID = ?");
                        int departmentId = switchScanner.nextInt();
                        try {
                            if (departmentId <= 0) {
                                throw new IllegalArgumentException();
                            }
                        } catch (IllegalArgumentException exception) {
                            logger.log(Level.SEVERE, "The option cannot be less than ZERO!", exception);
                            break;
                        }
                        query = entityManager.createQuery("select e.id,e.name, e.salary, e.hasDrivingLicense, e.isManager, " +
                                "e.startDate, e.endDate, e.active, e.address, e.postalCode, e.telephone, e.email, e.birthday, e.noChildren, " +
                                "e.studies, e.socialSecurityNumber from Employee e where department.id = ?1").setParameter(1, departmentId);

                        showEmployees(query);
                        audit.writePersonsToFile("Employees from department(ID = " + departmentId + ") listed ");
                    }
                    break;
                case 5:
                    break;
                default:
                    System.out.println("Not an available option.");
                    audit.writePersonsToFile("Application has ended. ");
                    break;
            }
        }
        entityManager.close();
    }

    private static void showEmployees(Query query) {
        List<Object[]> employeeList = query.getResultList();
        for (Object[] employeeIndex : employeeList) {

            Integer Id = (Integer) employeeIndex[0];
            String Name = (String) employeeIndex[1];
            Float Salary = (Float) employeeIndex[2];
            Boolean HasDrivingLicense = (Boolean) employeeIndex[3];
            Boolean isManager = (Boolean) employeeIndex[4];
            LocalDate startDate = (LocalDate) employeeIndex[5];
            LocalDate endDate = (LocalDate) employeeIndex[6];
            Boolean active = (Boolean) employeeIndex[7];
            String address = (String) employeeIndex[8];
            String postalCode = (String) employeeIndex[9];
            String telephone = (String) employeeIndex[10];
            String email = (String) employeeIndex[11];
            LocalDate birthday = (LocalDate) employeeIndex[12];
            Boolean noChildren = (Boolean) employeeIndex[13];
            String studies = (String) employeeIndex[14];
            String socialSecurityNumber = (String) employeeIndex[15];

            System.out.println("Employee: " + Id + ", " + Name + ", " + Salary + ", " + HasDrivingLicense +
                    ", " + isManager + ", " + startDate + ", " + endDate + "," + active + "," + address + ","
                    + postalCode + "," + telephone + "," + email + "," + birthday + "," + noChildren + "," +
                    studies + "," + socialSecurityNumber);
        }
    }

    //    @org.jetbrains.annotations.NotNull
    //    @org.jetbrains.annotations.Contract(pure = true)
    public static String setJql(int option) {
        switch (option) {
            case 1:
                return "select e.id,e.name, e.salary, e.hasDrivingLicense, e.isManager, e.startDate, e.endDate, " +
                        "e.active, e.address, e.postalCode, e.telephone, e.email, e.birthday, e.noChildren, " +
                        "e.studies, e.socialSecurityNumber from Employee e";
            case 2:
                return "select e.id,e.name, e.salary, e.hasDrivingLicense, e.isManager, e.startDate, e.endDate, " +
                        "e.active, e.address, e.postalCode, e.telephone, e.email, e.birthday, e.noChildren, " +
                        "e.studies, e.socialSecurityNumber from Employee e order by e.name, e.salary";
            case 3:
                return "select e.id,e.name, e.salary, e.hasDrivingLicense, e.isManager, e.startDate, e.endDate, " +
                        "e.active, e.address, e.postalCode, e.telephone, e.email, e.birthday, e.noChildren, " +
                        "e.studies, e.socialSecurityNumber from Employee e order by e.salary, e.name";
            default:
                return "";
        }
    }
}

