package com.cluster.platform.redis.dto;

import com.platform.common.util.StringUtil;

import java.io.Serializable;

/**
 * @author Advance
 * @date 2022年03月21日 14:19
 * @since V1.0.0
 */
public class SelectOptionDto implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3501580850682459680L;

    /**
     * 值
     */
    private String id;

    /**
     * 文本
     */
    private String text;

    /**
     * 拼音简称
     */
    private String col;

    /**
     * 父元素ID
     */
    private String parentId;

    private String institutionCode;

    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode == null ? null : institutionCode.trim();
    }

    /**
     * 对象（需要特殊处理的数据，对应html5 data-object属性）
     */
    private Object object;

    /**
     * 获取 id
     *
     * @return id.
     */
    public String getId() {
        return id;
    }

    /**
     * 设置 id
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取 text
     *
     * @return text.
     */
    public String getText() {
        return text;
    }

    /**
     * 设置 text
     *
     * @param text text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * 获取 col
     *
     * @return col.
     */
    public String getCol() {
        return col;
    }

    /**
     * 设置 col
     *
     * @param col col
     */
    public void setCol(String col) {
        this.col = col;
    }

    /**
     * 获取 parentId
     *
     * @return parentId.
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * 设置 parentId
     *
     * @param parentId parentId
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * 获取 object
     *
     * @return object.
     */
    public Object getObject() {
        return object;
    }

    /**
     * 设置 object
     *
     * @param object object
     */
    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        SelectOptionDto other = (SelectOptionDto)obj;
        if (!StringUtil.equals(id, other.id)) {
            return false;
        }

        if (!StringUtil.equals(parentId, other.parentId)) {
            return false;
        }

        if (!StringUtil.equals(text, other.text)) {
            return false;
        }

        return true;
    }

}
