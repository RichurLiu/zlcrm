package net.ziling.crm.dao;

import net.ziling.crm.entity.Duty;

public interface DutyMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table zc_duty
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(String dutyId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table zc_duty
     *
     * @mbggenerated
     */
    int insert(Duty record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table zc_duty
     *
     * @mbggenerated
     */
    int insertSelective(Duty record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table zc_duty
     *
     * @mbggenerated
     */
    Duty selectByPrimaryKey(String dutyId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table zc_duty
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(Duty record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table zc_duty
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(Duty record);
}