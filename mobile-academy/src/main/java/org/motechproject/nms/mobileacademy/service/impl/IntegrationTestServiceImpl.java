package org.motechproject.nms.mobileacademy.service.impl;

import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.repository.CourseDataService;
import org.motechproject.nms.mobileacademy.repository.LessonDataService;
import org.motechproject.nms.mobileacademy.repository.QuestionDataService;
import org.motechproject.nms.mobileacademy.service.IntegrationTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("itService")
public class IntegrationTestServiceImpl implements IntegrationTestService {

    @Autowired
    private CompletionRecordDataService completionRecordDataService;
    @Autowired
    private CourseDataService courseDataService;
    @Autowired
    private LessonDataService lessonDataService;
    @Autowired
    private QuestionDataService questionDataService;


    public void deleteAll() {
        completionRecordDataService.deleteAll();
        courseDataService.deleteAll();
        lessonDataService.deleteAll();
        questionDataService.deleteAll();
    }
}
