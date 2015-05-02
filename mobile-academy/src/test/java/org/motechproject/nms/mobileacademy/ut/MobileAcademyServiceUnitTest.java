package org.motechproject.nms.mobileacademy.ut;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.mtraining.repository.BookmarkDataService;
import org.motechproject.nms.mobileacademy.domain.Course;
import org.motechproject.nms.mobileacademy.repository.CourseDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.nms.mobileacademy.service.impl.MobileAcademyServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for Mobile Academy Service
 */
public class MobileAcademyServiceUnitTest {

    @Mock
    private MobileAcademyService mobileAcademyService;

    @Mock
    private CourseDataService courseDataService;

    @Mock
    private BookmarkDataService bookmarkDataService;

    @Before
    public void setup() {
        initMocks(this);
        mobileAcademyService = new MobileAcademyServiceImpl(bookmarkDataService, courseDataService);
    }

    @Test
    public void getCourseTest() {
        Course currentCourse = new Course();
        when(courseDataService.findCourseByName("MobileAcademyCourse")).thenReturn(currentCourse);
        assertEquals(mobileAcademyService.getCourse(), currentCourse);
    }
}
