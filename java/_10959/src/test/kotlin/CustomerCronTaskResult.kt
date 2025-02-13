package kotlin

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
//@JsonSubTypes(
//    JsonSubTypes.Type(value = CustomerCronTaskResult::class, name = "customerCronTaskResult")
//)
//open interface ICustomerCronTaskResult{
//
//}


public class CustomerCronTaskResult(listOf: List<String>) {
    public val team: List<String> = listOf
    //create empty constructor
    constructor() : this(listOf())
}
