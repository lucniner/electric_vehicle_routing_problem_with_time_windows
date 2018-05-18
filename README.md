# electric_vehicle_routing_problem_with_time_windows
Electric-Vehicle Routing Problem with Time Windows (EVRPTW) for the optimization in transports and logistics coure summer term 2018 at the technical university of vienna


# Pseudo code construction heuristic

* get the customer map customer -> potential neighbours from the distanceholder (getInterCustomerDistances)
* iterate over these customers
* if one customer is used for a route - track that information so that when merging applies customers do not get used twice
* if the customer has no potential neighbours (pendel route) check if a recharging is needed on the way back
* if the customer has potential neighbours, iterate over all of them and check
    * if the energy is still sufficient (calculate the used power and use that)
        * insert next customer
        * calculate the remaining power and track the time
        * when the next customer is to be served use the remaining battery and the new time
    * if not check if with the nearest recharging station the time window can be full filled
        * if yes take the charging station and calculate time offset due to recharging
        * if no try next customer in list
       