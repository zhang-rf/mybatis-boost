package cn.mybatisboost.test;

public class Project {

    private Integer id;
    private String groupId;
    private String artifactId;
    private String license;
    private String scm;
    private String developer;

    public Project() {
    }

    public Project(String groupId, String artifactId, String license, String scm, String developer) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.license = license;
        this.scm = scm;
        this.developer = developer;
    }

    public Integer getId() {
        return id;
    }

    public Project setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public Project setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public Project setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public String getLicense() {
        return license;
    }

    public Project setLicense(String license) {
        this.license = license;
        return this;
    }

    public String getScm() {
        return scm;
    }

    public Project setScm(String scm) {
        this.scm = scm;
        return this;
    }

    public String getDeveloper() {
        return developer;
    }

    public Project setDeveloper(String developer) {
        this.developer = developer;
        return this;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", license='" + license + '\'' +
                ", scm='" + scm + '\'' +
                ", developer='" + developer + '\'' +
                '}';
    }
}
