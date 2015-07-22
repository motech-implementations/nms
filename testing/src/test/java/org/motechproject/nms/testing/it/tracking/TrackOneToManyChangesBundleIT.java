package org.motechproject.nms.testing.it.tracking;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.testing.tracking.domain.Department;
import org.motechproject.nms.testing.tracking.domain.Employee;
import org.motechproject.nms.testing.tracking.repository.DepartmentDataService;
import org.motechproject.nms.testing.tracking.repository.EmployeeDataService;
import org.motechproject.nms.tracking.domain.ChangeLog;
import org.motechproject.nms.tracking.repository.ChangeLogDataService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class TrackOneToManyChangesBundleIT extends BasePaxIT {

    @Inject
    EmployeeDataService employeeDataService;

    @Inject
    DepartmentDataService departmentDataService;

    @Inject
    ChangeLogDataService changeLogDataService;

    @Before
    public void setUp() {
        employeeDataService.deleteAll();
        departmentDataService.deleteAll();
        changeLogDataService.deleteAll();
    }

    @Test
    public void testChangesTrackedForInstanceCreation() {
        Employee johnDoe = createEmployeeInTransaction("Jonh Doe", null);
        Employee janeRoe = createEmployeeInTransaction("Jane Roe", null);
        Department department = createDepartmentInTransaction("Sales", johnDoe, janeRoe);

        List<ChangeLog> changes = getDepartmentChangeLogs(department);
        assertEquals(1, changes.size());
        String change = changes.get(0).getChange();
        assertThat(change, Matchers.containsString("name(null, Sales)"));
        assertThat(change, Matchers.containsString(String.format("employees(added[%d,%d])", johnDoe.getId(), janeRoe.getId())));
    }

    @Test
    public void testChangesTrackedForInstanceUpdate() {
        Employee johnDoe = createEmployeeInTransaction("Jonh Doe", null);
        Employee janeRoe = createEmployeeInTransaction("Jane Roe", null);
        Employee williamJones = createEmployeeInTransaction("William Jones", null);
        Employee marySmith = createEmployeeInTransaction("Mary Smith", null);
        Employee henryTaylor = createEmployeeInTransaction("Henry Taylor", null);
        Department sales;
        sales = createDepartmentInTransaction("Sals", johnDoe, janeRoe, williamJones);
        sales = updateDepartmentInTransaction(sales.getId(), "Sales", johnDoe, janeRoe, marySmith, henryTaylor);

        List<ChangeLog> changes = getDepartmentChangeLogs(sales);
        assertEquals(2, changes.size());
        String change = getLatestChangeLog(changes).getChange();
        assertThat(change, Matchers.containsString("name(Sals, Sales)"));
        assertThat(change, Matchers.containsString(String.format("employees(added[%d,%d], removed[%d])", marySmith.getId(), henryTaylor.getId(), williamJones.getId())));
    }

    @Test
    public void testChangesTrackedForCollectionManipulations() {
        Employee johnDoe = createEmployeeInTransaction("Jonh Doe", null);
        Employee janeRoe = createEmployeeInTransaction("Jane Roe", null);
        final Employee williamJones = createEmployeeInTransaction("William Jones", null);
        final Employee marySmith = createEmployeeInTransaction("Mary Smith", null);
        final Employee henryTaylor = createEmployeeInTransaction("Henry Taylor", null);
        final Department sales = createDepartmentInTransaction("Sals", johnDoe, janeRoe, williamJones);
        departmentDataService.doInTransaction(new TransactionCallback<Department>() {
            @Override
            public Department doInTransaction(TransactionStatus transactionStatus) {
                Department department = departmentDataService.findById(sales.getId());
                department.setName("Sales");
                department.getEmployees().remove(marySmith); // removing non existing item
                department.getEmployees().remove(henryTaylor); // removing non existing item
                department.getEmployees().addAll(Arrays.asList(marySmith, henryTaylor));
                department.getEmployees().remove(williamJones);
                return departmentDataService.update(department);
            }
        });

        List<ChangeLog> changes = getDepartmentChangeLogs(sales);
        assertEquals(2, changes.size());
        String change = getLatestChangeLog(changes).getChange();
        assertThat(change, Matchers.containsString("name(Sals, Sales)"));
        assertThat(change, Matchers.containsString(String.format("employees(added[%d,%d], removed[%d])", marySmith.getId(), henryTaylor.getId(), williamJones.getId())));
    }

    @Test
    public void testChangesTrackedForReassignedDependentInstance() {
        Employee johnDoe = createEmployeeInTransaction("Jonh Doe", null);
        Employee janeRoe = createEmployeeInTransaction("Jane Roe", null);
        Employee williamJones = createEmployeeInTransaction("William Jones", null);
        Department sales = createDepartmentInTransaction("Sales", johnDoe, janeRoe, williamJones);
        Department marketing = createDepartmentInTransaction("Marketing", johnDoe, janeRoe);

        List<ChangeLog> changes = getDepartmentChangeLogs(marketing);
        assertEquals(1, changes.size());
        String change = changes.get(0).getChange();
        assertThat(change, Matchers.containsString("name(null, Marketing)"));
        assertThat(change, Matchers.containsString(String.format("employees(added[%d,%d])", johnDoe.getId(), janeRoe.getId())));
    }

    private Department createDepartmentInTransaction(final String name, final Employee... employees) {
        return departmentDataService.doInTransaction(new TransactionCallback<Department>() {
            @Override
            public Department doInTransaction(TransactionStatus transactionStatus) {
                Department department = new Department();
                department.setName(name);
                department.setEmployees(Arrays.asList(employees));
                return departmentDataService.create(department);
            }
        });
    }

    private Department updateDepartmentInTransaction(final Long id, final String name, final Employee... employees) {
        return departmentDataService.doInTransaction(new TransactionCallback<Department>() {
            @Override
            public Department doInTransaction(TransactionStatus transactionStatus) {
                Department department = departmentDataService.findById(id);
                department.setName(name);
                department.setEmployees(Arrays.asList(employees));
                return departmentDataService.update(department);
            }
        });
    }

    private Employee createEmployeeInTransaction(final String name, final Department department) {
        return employeeDataService.doInTransaction(new TransactionCallback<Employee>() {
            @Override
            public Employee doInTransaction(TransactionStatus transactionStatus) {
                Employee employee = new Employee();
                employee.setName(name);
                employee.setDepartment(department);
                return employeeDataService.create(employee);
            }
        });
    }

    private List<ChangeLog> getDepartmentChangeLogs(Department department) {
        return changeLogDataService.findByEntityNameAndInstanceId(Department.class.getName(), department.getId());
    }

    private ChangeLog getLatestChangeLog(List<ChangeLog> changes) {
        return Collections.max(changes, new Comparator<ChangeLog>() {
            @Override
            public int compare(ChangeLog o1, ChangeLog o2) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        });
    }
}
