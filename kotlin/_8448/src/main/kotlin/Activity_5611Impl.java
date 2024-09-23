public class Activity_5611Impl implements Activity_5611 {
    @Override
    public String doSomething() {

        if(true){
            throw new RuntimeException(">>>>>  ");
        }

        System.out.println("Running activity >>>>> ");
        return "something";
    }
}