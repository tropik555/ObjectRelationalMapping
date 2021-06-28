package Client;

import Client.Entities.Animal;
import Client.Entities.Zoo;
import OrmArchivarius.Manager.OrmManager;

import java.util.*;


public class Application {

    public static void main(String[] args) throws Exception {
        // Connection connection = ConnectionFactory.getConnection();
        // System.out.println(connection);
   /*     String rootPath = Thread.currentThread().getContextClassLoader().getResource("database.properties").getPath();
        System.out.println(rootPath);*/
        OrmManager ormManager = OrmManager.get("zoo"); // by database to work with
        ormManager.scanPackages(Application.class);


        System.out.println("--------------TEST------------------------------------");
        Animal animal1 = new Animal("donkey", 2);
        Animal animal2 = new Animal("eagle", 4);

        System.out.println(animal1.getId()); // >> null

        ormManager.save(animal1);
        ormManager.save(animal2);

        Long id1 = animal1.getId();
        System.out.println(id1); // >> 45


        animal1.setAge(animal1.getAge() + 1);
        System.out.println(animal1.getAge()); // >> 3
        ormManager.update(animal1);

        Animal animal1_1 = ormManager.getById(Animal.class, id1)
                .orElseThrow(NoSuchElementException::new);
        System.out.println(animal1_1.equals(animal1)); // >> true
        System.out.println(animal1_1 == animal1); // >> false or true, depends on implementation



        animal1.setAge(animal1.getAge() + 1);
        Animal animal1_2 = ormManager.getById(Animal.class, id1)
                .orElseThrow(NoSuchElementException::new);;
        System.out.println(animal1_2.equals(animal1)); // >> false

        ormManager.update(animal1);
        List<Animal> allAnimals = ormManager.getAll(Animal.class);
        System.out.println(allAnimals);

        System.out.println(allAnimals.size());//20
        System.out.println(animal1.equals(allAnimals.get(0)));
        System.out.println(animal1);
        System.out.println(allAnimals.get(0).toString());

        ormManager.delete(animal1);
        System.out.println(animal1.getId());
        Optional<Animal> animal1_3 = ormManager.getById(Animal.class, id1);
        System.out.println(animal1_3.isEmpty());
        System.out.println(animal1_3.isPresent());


        Zoo zoo = new Zoo("Manchester");
        Zoo zoo1 = new Zoo("London");
        ormManager.save(zoo);
        ormManager.save(zoo1);

        List<Zoo> zoos = ormManager.getAll(Zoo.class);
        System.out.println(zoos);
        System.out.println(zoos.get(0).equals(zoos.get(1)));

        //TODO TEST
/*        OrmManager ormManager = OrmManager.get("DBname"); // by database to work with
        ormManager.scanPackages(Application.class);

        Animal animal1 = new Animal("Cukaracha", 2);
        System.out.println(animal1.getId()); // >> null

        ormManager.save(animal1);
        Long id1 = animal1.getId();
        System.out.println(id1); // >> 1

        animal1.setAge(animal1.getAge() + 1);
        System.out.println(animal1.getAge()); // >> 3
        ormManager.update(animal1);

        Animal animal1_1 = ormManager.getById(Animal.class, id1)
                .orElseThrow(NoSuchElementException::new);
        System.out.println(animal1_1.equals(animal1)); // >> true
        System.out.println(animal1_1 == animal1); // >> false or true, depends on implementation

        animal1.setAge(animal1.getAge() + 1);
        Animal animal1_2 = ormManager.getById(Animal.class, id1)
                .orElseThrow(NoSuchElementException::new);;
        System.out.println(animal1_2.equals(animal1)); // >> false

        ormManager.update(animal1);
        List<Animal> allAnimals = ormManager.getAll(Animal.class);
        System.out.println(allAnimals.size());
        System.out.println(animal1.equals(allAnimals.get(0)));

        ormManager.delete(animal1);
        System.out.println(animal1.getId());
        Optional<Animal> animal1_3 = ormManager.getById(Animal.class, id1);
        System.out.println(animal1_3.isEmpty());*/
    }
}
