package net.weg.capture.constant.errors.project;

public final class PackageErrors {

    private PackageErrors() {
    }

    public static final String PACKAGE_NAME_REQUIRED = "Package name is required";
    public static final String PACKAGE_ESTIMATED_VALUE_REQUIRED = "Package estimated value is required";
    public static final String PACKAGE_YEAR_REQUIRED = "Package year is required";
    public static final String PACKAGE_QUARTER_REQUIRED = "Package quarter is required";
    public static final String PACKAGE_STATUS_REQUIRED = "Package status is required";
    public static final String PACKAGE_LIST_CANNOT_BE_EMPTY = "The list of packages cannot be empty.";

    public static final String PACKAGE_NOT_FOUND = "Project Package not found";

    public static final String PROJECT_PACKAGE_RELEVANT_BUSINESS_UNIT_NOT_FOUND = "Project Package Relevant Business Unit not found";
    public static final String BUSINESS_UNIT_AND_CENTER_BOTH_PROVIDED = "Business Unit and Business Center cannot be provided at the same time.";

    public static final String BUSINESS_UNIT_OR_CENTER_REQUIRED = "Either Business Unit or Business Center must be provided.";

    public static final String BUSINESS_CENTER_WITH_EXTRA_FIELDS = "If Business Center is provided, Product Group and Factory must be null.";
    public static final String PACKAGE_FORBIDDEN = "You do not have permission to perform this action.";
}