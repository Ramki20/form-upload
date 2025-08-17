package gov.usda.fsa.fcao.flp.ola.core.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Data;

@Table(schema = "qstn_ans", name = "oper_prfl_cat")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
// Remove @Builder annotation
public class OperationProfileCategoryPlain extends AuditedEntity {

    @Id
    @Column("oper_prfl_cat_id")
    private Integer operationProfileCategoryIdentifier;

    @NotNull(message = "Application Borrower Identifier is mandatory.")
    @Column("app_borr_id")
    private Integer applicationBorrowerIdentifier;

    @NotBlank(message = "Data status code is mandatory.")
    @Column("data_stat_cd")
    private String dataStatusCode;

    @NotBlank(message = "Enterprise type code is mandatory.")
    @Column("entp_type_cd")
    private String enterpriseTypeCode;

    @NotBlank(message = "Operation category code is mandatory.")
    @Column("oper_cat_cd")
    private String operationCategoryCode;

    @NotNull(message = "Creation date is mandatory.")
    @Column("cre_dt")
    private Date creationDate;

    @NotBlank(message = "Creation user name is mandatory.")
    @Column("cre_user_nm")
    private String creationUserName;

    @NotNull(message = "Last change date is mandatory.")
    @Column("last_chg_dt")
    private Date lastChangeDate;

    @NotBlank(message = "Last change user name is mandatory.")
    @Column("last_chg_user_nm")
    private String lastChangeUserName;

    // Transient fields
    @Transient
    private Set<OperationProfileSubCategory> operationProfileSubCategorySet = new HashSet<>();

    // Default constructor for Spring Data JDBC
    public OperationProfileCategoryPlain() {
        this.operationProfileSubCategorySet = new HashSet<>();
    }

    // Builder pattern implementation
    public static OperationProfileCategoryPlainBuilder builder() {
        return new OperationProfileCategoryPlainBuilder();
    }

    public OperationProfileCategoryPlainBuilder toBuilder() {
        return new OperationProfileCategoryPlainBuilder(this);
    }

    // Custom builder class with only persistent fields
    public static class OperationProfileCategoryPlainBuilder {
        private OperationProfileCategoryPlain instance;

        public OperationProfileCategoryPlainBuilder() {
            this.instance = new OperationProfileCategoryPlain();
        }

        public OperationProfileCategoryPlainBuilder(OperationProfileCategoryPlain existing) {
            this.instance = new OperationProfileCategoryPlain();
            // Copy persistent fields from existing instance
            this.instance.setOperationProfileCategoryIdentifier(existing.getOperationProfileCategoryIdentifier());
            this.instance.setApplicationBorrowerIdentifier(existing.getApplicationBorrowerIdentifier());
            this.instance.setDataStatusCode(existing.getDataStatusCode());
            this.instance.setEnterpriseTypeCode(existing.getEnterpriseTypeCode());
            this.instance.setOperationCategoryCode(existing.getOperationCategoryCode());
            this.instance.setCreationDate(existing.getCreationDate());
            this.instance.setCreationUserName(existing.getCreationUserName());
            this.instance.setLastChangeDate(existing.getLastChangeDate());
            this.instance.setLastChangeUserName(existing.getLastChangeUserName());
        }

        // Basic field builders
        public OperationProfileCategoryPlainBuilder operationProfileCategoryIdentifier(Integer operationProfileCategoryIdentifier) {
            instance.setOperationProfileCategoryIdentifier(operationProfileCategoryIdentifier);
            return this;
        }

        public OperationProfileCategoryPlainBuilder applicationBorrowerIdentifier(Integer applicationBorrowerIdentifier) {
            instance.setApplicationBorrowerIdentifier(applicationBorrowerIdentifier);
            return this;
        }

        public OperationProfileCategoryPlainBuilder dataStatusCode(String dataStatusCode) {
            instance.setDataStatusCode(dataStatusCode);
            return this;
        }

        public OperationProfileCategoryPlainBuilder enterpriseTypeCode(String enterpriseTypeCode) {
            instance.setEnterpriseTypeCode(enterpriseTypeCode);
            return this;
        }

        public OperationProfileCategoryPlainBuilder operationCategoryCode(String operationCategoryCode) {
            instance.setOperationCategoryCode(operationCategoryCode);
            return this;
        }

        public OperationProfileCategoryPlainBuilder creationDate(Date creationDate) {
            instance.setCreationDate(creationDate);
            return this;
        }

        public OperationProfileCategoryPlainBuilder creationUserName(String creationUserName) {
            instance.setCreationUserName(creationUserName);
            return this;
        }

        public OperationProfileCategoryPlainBuilder lastChangeDate(Date lastChangeDate) {
            instance.setLastChangeDate(lastChangeDate);
            return this;
        }

        public OperationProfileCategoryPlainBuilder lastChangeUserName(String lastChangeUserName) {
            instance.setLastChangeUserName(lastChangeUserName);
            return this;
        }

        // Build method
        public OperationProfileCategoryPlain build() {
            return instance;
        }
    }

    // Keep your existing methods
    public void addOperationProfileSubCategory(OperationProfileSubCategory operationProfileSubCategory) {
        if (this.operationProfileSubCategorySet == null) {
            this.operationProfileSubCategorySet = new HashSet<>();
        }
        operationProfileSubCategorySet.add(operationProfileSubCategory);
    }
}