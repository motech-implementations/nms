package org.motechproject.nms.mobileacademy.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.flw.domain.FrontLineWorker;

import java.util.HashMap;

@Entity(tableName = "nms_flw_course_module")
public class FlwModule {

    @Field
    private Long externalId;

    @Field
    private Integer chapter01;

    @Field
    private Integer chapter02;

    @Field
    private Integer chapter03;

    @Field
    private Integer chapter04;

    @Field
    private Integer chapter05;

    @Field
    private Integer chapter06;

    @Field
    private Integer chapter07;

    @Field
    private Integer chapter08;

    @Field
    private Integer chapter09;

    @Field
    private Integer chapter10;

    @Field
    private Integer chapter11;

    @Field
    private Integer chapter12;

    @Field
    private Integer feedback;

    @Field
    private Integer chapter13;

    @Field
    private Integer chapter14;

    @Field
    private Integer chapter15;

    @Field
    private Integer chapter16;

    @Field
    private Integer chapter17;

    @Field
    private Integer chapter18;

    @Field
    private Integer chapter19;

    @Field
    private Integer chapter20;

    @Field
    private Integer chapter21;

    @Field
    private Integer chapter22;

    @Field
    private Integer chapter23;

    @Field
    private Integer chapter24;

    public Long getExternalId() {
        return externalId;
    }

    public Integer getFeedback() {
        return feedback;
    }

    public Integer getChapter01() {
        return chapter01;
    }

    public Integer getChapter02() {
        return chapter02;
    }

    public Integer getChapter03() {
        return chapter03;
    }

    public Integer getChapter04() {
        return chapter04;
    }

    public Integer getChapter05() {
        return chapter05;
    }

    public Integer getChapter06() {
        return chapter06;
    }

    public Integer getChapter07() {
        return chapter07;
    }

    public Integer getChapter08() {
        return chapter08;
    }

    public Integer getChapter09() {
        return chapter09;
    }

    public Integer getChapter10() {
        return chapter10;
    }

    public Integer getChapter11() {
        return chapter11;
    }

    public Integer getChapter12() {
        return chapter12;
    }

    public Integer getChapter13() {
        return chapter13;
    }

    public Integer getChapter14() {
        return chapter14;
    }

    public Integer getChapter15() {
        return chapter15;
    }

    public Integer getChapter16() {
        return chapter16;
    }

    public Integer getChapter17() {
        return chapter17;
    }

    public Integer getChapter18() {
        return chapter18;
    }

    public Integer getChapter19() {
        return chapter19;
    }

    public Integer getChapter20() {
        return chapter20;
    }

    public Integer getChapter21() {
        return chapter21;
    }

    public Integer getChapter22() {
        return chapter22;
    }

    public Integer getChapter23() {
        return chapter23;
    }

    public Integer getChapter24() {
        return chapter24;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public void setFeedback(Integer feedback) {
        this.feedback = feedback;
    }

    public void setChapter01(Integer chapter01) {
        this.chapter01 = chapter01;
    }

    public void setChapter02(Integer chapter02) {
        this.chapter02 = chapter02;
    }

    public void setChapter03(Integer chapter03) {
        this.chapter03 = chapter03;
    }

    public void setChapter04(Integer chapter04) {
        this.chapter04 = chapter04;
    }

    public void setChapter05(Integer chapter05) {
        this.chapter05 = chapter05;
    }

    public void setChapter06(Integer chapter06) {
        this.chapter06 = chapter06;
    }

    public void setChapter07(Integer chapter07) {
        this.chapter07 = chapter07;
    }

    public void setChapter08(Integer chapter08) {
        this.chapter08 = chapter08;
    }

    public void setChapter09(Integer chapter09) {
        this.chapter09 = chapter09;
    }

    public void setChapter10(Integer chapter10) {
        this.chapter10 = chapter10;
    }

    public void setChapter11(Integer chapter11) {
        this.chapter11 = chapter11;
    }

    public void setChapter12(Integer chapter12) {
        this.chapter12 = chapter12;
    }

    public void setChapter13(Integer chapter13) {
        this.chapter13 = chapter13;
    }

    public void setChapter14(Integer chapter14) {
        this.chapter14 = chapter14;
    }

    public void setChapter15(Integer chapter15) {
        this.chapter15 = chapter15;
    }

    public void setChapter16(Integer chapter16) {
        this.chapter16 = chapter16;
    }

    public void setChapter17(Integer chapter17) {
        this.chapter17 = chapter17;
    }

    public void setChapter18(Integer chapter18) {
        this.chapter18 = chapter18;
    }

    public void setChapter19(Integer chapter19) {
        this.chapter19 = chapter19;
    }

    public void setChapter20(Integer chapter20) {
        this.chapter20 = chapter20;
    }

    public void setChapter21(Integer chapter21) {
        this.chapter21 = chapter21;
    }

    public void setChapter22(Integer chapter22) {
        this.chapter22 = chapter22;
    }

    public void setChapter23(Integer chapter23) {
        this.chapter23 = chapter23;
    }

    public void setChapter24(Integer chapter24) {
        this.chapter24 = chapter24;
    }

    public FlwModule () {

    }

    public FlwModule (Long externalId, Integer feedback){
        this.externalId = externalId;
        this.feedback = feedback;
    }

}
