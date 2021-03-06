package OrmArchivarius.Manager;

import OrmArchivarius.Annotations.Column;
import OrmArchivarius.Annotations.Entity;
import OrmArchivarius.Annotations.Id;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

public class OrmManager {
    private static final String SEPARATOR = ",";

    private static Connection connection;
    private static Statement statement = null;
    private static PreparedStatement preparedStatement = null;

    public static OrmManager get(String dbName) {
        connection=ConnectionFactory.getConnection(dbName);
        return new OrmManager();
    }

    public void scanPackages(Class<?> rootClass) throws SQLException {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                // .setUrls(ClasspathHelper.forPackage("Client.Entities"))
                .setUrls(ClasspathHelper.forClass(rootClass))
                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));
        Class[] arrayOfClasses =
                reflections.getTypesAnnotatedWith(Entity.class).toArray(new Class[0]);

        String[] names = new String[arrayOfClasses.length];
        for (int i = 0; i < arrayOfClasses.length; i++) {
            names[i] = arrayOfClasses[i].toString().replaceAll("^class [A-Z][a-z]+[.][A-Z][a-z]+[.]", "");
        }


        String s = "";
        for (int i = 0; i < arrayOfClasses.length; i++) {
            Class<?> clazz = arrayOfClasses[i];
            Field[] fields = clazz.getDeclaredFields();
            String result = receiveSQlQuery(fields);
            s += createIfNotExist + " " + names[i] + result;
            createTable(s);
        }
        connection.commit();
        //  Reflections reflections = new Reflections("my.project.prefix");
        // find all the classes marked with @Entity in the subpackages

        //Reflections reflections = new Reflections("my.project");
        // return s;
    }

    private static String[] getValues(Object o) throws IllegalAccessException {
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldsValues = new String[fields.length];
        for (int i = 1; i < fields.length; i++) {
            fields[i].setAccessible(true);
            fieldsValues[i] = fields[i].get(o).toString();
        }
        return fieldsValues;
    }

    public void save(Object o) throws IllegalAccessException {
        String objectName = o.getClass().getSimpleName();
        String[] fieldsValues = getValues(o);
        try {
            if (objectName.equals("Animal")) {
                preparedStatement = connection.prepareStatement(AnimalmodelSQL.SAVE.A_QUERY, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, fieldsValues[1]);
                preparedStatement.setInt(2, Integer.parseInt(fieldsValues[2]));
            } else {
                preparedStatement = connection.prepareStatement(Zoo.ADD.Z_QUERY, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, fieldsValues[1]);

            }
            preparedStatement.executeUpdate();


            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            Method setIdmethod = o.getClass().getDeclaredMethod("setId", Long.class);
            long id = 0;
            if (resultSet.next()) {
                id = resultSet.getLong(1);
            }
            setIdmethod.invoke(o, id);
            connection.commit();
            preparedStatement.close();
        } catch (SQLException | NoSuchMethodException | InvocationTargetException throwables) {
            throwables.printStackTrace();
        }

        //String  res=new String()
        // save the object to DB
        // get back the autogenerated Id
        // set the id for the object

    }

    public void update(Object o) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String[] fieldsValues = getValues(o);
        PreparedStatement preparedStatement;
        String objectType = o.getClass().getSimpleName();
        try {

            if (objectType.equals("Animal")) {
                preparedStatement = connection.prepareStatement(AnimalmodelSQL.UPDATE.A_QUERY);
                preparedStatement.setString(1, fieldsValues[1]);
                preparedStatement.setInt(2, Integer.parseInt(fieldsValues[2]));
                preparedStatement.setLong(3, getId(o));
            } else {
                preparedStatement = connection.prepareStatement(Zoo.UPDATE.Z_QUERY);
                preparedStatement.setString(1, fieldsValues[1]);
                preparedStatement.setLong(2, getId(o));
            }

            preparedStatement.executeUpdate();
            connection.commit();
            preparedStatement.close();
        } catch (SQLException | NoSuchFieldException throwables) {
            throwables.printStackTrace();
        }
    }

    public static Long getId(Object o) throws IllegalAccessException, SQLException, NoSuchFieldException {
        Field field = o.getClass().getDeclaredField("id");
        field.setAccessible(true);
        Object value = field.get(o);
        return (Long) value;
    }

    public <T> Optional<T> getById(Class<T> clazz, Long id1) throws Exception {
        String s = clazz.getSimpleName();
        String sqlQuery = String.format(commonQueries.GETbyId.QUERY, s);
        Optional<T> optionalT = Optional.empty();
        ResultSet resultSet;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setLong(1, id1);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Object o = clazz.getDeclaredConstructor().newInstance();
                if (s.equals("Animal")) {
                    Method method = clazz.getMethod("setId", Long.class);
                    method.invoke(o, resultSet.getLong("id"));
                    Method method1 = clazz.getMethod("setName", String.class);
                    method1.invoke(o, resultSet.getString("name"));
                    Method method2 = clazz.getMethod("setAge", int.class);
                    method2.invoke(o, resultSet.getInt("age"));
                } else {
                    Method method = clazz.getMethod("setId", Long.class);
                    method.invoke(o, resultSet.getLong("id"));
                    Method method1 = clazz.getMethod("setAddress", String.class);
                    method1.invoke(o, resultSet.getString("address"));
                }
                optionalT = (Optional<T>) Optional.ofNullable(o);
                //  list.add((T) o);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return optionalT;

    }

    public <T> List<T> getAll(Class<T> clazz) throws Exception {
        String s = clazz.getSimpleName();
        String sqlQuery = String.format(commonQueries.GETall.QUERY, s);
        ResultSet resultSet = null;
        ArrayList<T> list=null;

        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlQuery);
            list=new ArrayList<>(resultSet.getFetchSize());
          //  Object o = clazz.getDeclaredConstructor().newInstance();

            while (resultSet.next()) {
                Object o = clazz.getDeclaredConstructor().newInstance();

                if (s.equals("Animal")) {

                    Method method = clazz.getMethod("setId", Long.class);
                    method.invoke(o, resultSet.getLong("id"));
                    Method method1 = clazz.getMethod("setName", String.class);
                    method1.invoke(o, resultSet.getString("name"));
                    Method method2 = clazz.getMethod("setAge", int.class);
                    method2.invoke(o, resultSet.getInt("age"));
                } else {
                    Method method = clazz.getMethod("setId", Long.class);
                    method.invoke(o, resultSet.getLong("id"));
                    Method method1 = clazz.getMethod("setAddress", String.class);
                    method1.invoke(o, resultSet.getString("address"));
                }
                list.add((T) o);


            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return list;
    }

    public boolean delete(Object object) throws SQLException {
        Field[] fields = object.getClass().getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);
        String objectName = object.getClass().getSimpleName();
        PreparedStatement preparedStatement;
        try {
            if (objectName.equals("Animal")) {
                preparedStatement = connection.prepareStatement(AnimalmodelSQL.DELETE.A_QUERY);
                preparedStatement.setString(1, fields[1].get(object).toString());
                preparedStatement.setInt(2, fields[2].getInt(object));
            } else {
                preparedStatement = connection.prepareStatement(Zoo.DELETE.Z_QUERY);
                preparedStatement.setString(1, fields[1].get(object).toString());
            }

            preparedStatement.execute();
            connection.commit();
            preparedStatement.close();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return true; // stub vs mock
    }

    private static void createTable(String string) throws SQLException {
        statement = connection.createStatement();
        statement.execute(string);
    }

    private static String receiveSQlQuery(Field[] fields) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Field f : fields) {
            stringBuilder.append(convertClassFieldsToSqlQuery(f));
        }
        return stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "").append(");").replace(0, 0, "(").toString();
    }

    private static String convertClassFieldsToSqlQuery(Field field) {
        StringBuilder SQLstatement = new StringBuilder();
        if (field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
            SQLstatement.append(field.getName()).append(" SERIAL PRIMARY KEY").append(",");
        }
        if (!field.isAnnotationPresent(Id.class)) {
            SQLstatement.append(" " + field.getName()).append(" ").append(convertJavaTypeIntoSqlType(field.getType().toString())).append(",");
        }

        return SQLstatement.toString();
    }

    private static String convertJavaTypeIntoSqlType(String string) {
        if (string.endsWith("Byte") || string.endsWith("Short") || string.endsWith("Integer") || string.endsWith("Long")
                || string.endsWith("byte") || string.endsWith("short") || string.endsWith("int") || string.endsWith("long")) {
            string = "bigint";
        } else if (string.endsWith("String")) {
            string = "text";
        }

        return string;
    }


    enum AnimalmodelSQL {
        GETall("SELECT * FROM animal"),
        GetByID("SELECT * FROM animal WHERE id = (?)"),
        DELETE("DELETE FROM animal WHERE name= (?) AND age= (?)"),
        getId("Select id FROM animal WHERE name=(?) and age=(?) LIMIT 1"),
        SAVE("INSERT INTO animal (id, name, age) VALUES (DEFAULT, (?),(?)) RETURNING id"),
        UPDATE("UPDATE animal SET name = (?), age = (?) WHERE id = (?)");
        String A_QUERY;

        AnimalmodelSQL(String A_QUERY) {
            this.A_QUERY = A_QUERY;
        }
    }

    enum Zoo {
        GETall("SELECT * FROM ZOO"),
        GetByID("SELECT * FROM ZOO WHERE ZOO_ID = (?)"),
        DELETE("DELETE FROM zoo WHERE address= (?)"),
        ADD("INSERT INTO zoo (id, address) VALUES (DEFAULT, (?)) RETURNING id;"),
        UPDATE("UPDATE zoo SET address = (?) WHERE id = (?) ");
        String Z_QUERY;

        Zoo(String Z_QUERY) {
            this.Z_QUERY = Z_QUERY;
        }
    }

    enum commonQueries {
        GETall("SELECT * FROM %s ORDER BY id"),
        GETbyId("SELECT * FROM %s WHERE id = (?)");
        String QUERY;

        commonQueries(String QUERY) {
            this.QUERY = QUERY;
        }
    }

    private static final String SQL_CREATE_ZOO = "CREATE TABLE ZOO"
            + "("
            + " ZOO_ID serial,"
            + " address text NOT NULL,"
            + " PRIMARY KEY (ZOO_ID)"
            + ")";

    private static final String SQL_CREATE_ANIMALS = "CREATE TABLE ANIMAL"
            + "("
            + " ZOO_ID serial,"
            + " ANIMAL_ID serial,"
            + " name text NOT NULL,"
            + "age integer NOT NULL,"
            + " PRIMARY KEY (ZOO_ID,ANIMAL_ID),"
            + "FOREIGN KEY (ZOO_ID)"
            + "REFERENCES ZOO (ZOO_ID)"
            + "ON DELETE CASCADE"
            + ")";

    private static final String createIfNotExist = "CREATE TABLE IF NOT EXISTS";

}
