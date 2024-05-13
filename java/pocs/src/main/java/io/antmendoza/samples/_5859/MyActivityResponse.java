package io.antmendoza.samples._5859;

public class MyActivityResponse {

    String name;
    String name2;

    public MyActivityResponse(final String name, final String name2) {
        this.name = name;
        this.name2 = name2;
    }


    public MyActivityResponse() {


    }


    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }


    public String getName2() {
        return name2;
    }

    public void setName2(final String name2) {
        this.name2 = name2;
    }

    @Override
    public String toString() {
        return "MyActivityResponse{" +
                "name='" + name + '\'' +
                ", name2='" + name2 + '\'' +
                '}';
    }
}
