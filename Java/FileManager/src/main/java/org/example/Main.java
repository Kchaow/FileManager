package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    private static final Path DEFAULT_FILE_PATH = Paths.get(System.getProperty("user.home"));
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        System.loadLibrary("volume_info");
    }

    public static void main(String[] args) throws JAXBException {
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\\s");
        boolean isExit = false;
        while (!isExit) {
            printOptions();
            int choice = inputChoice(scanner);
            switch (choice) {
                case 1:
                    createFile(scanner);
                    break;
                case 2:
                    printVolumesInfo();
                    break;
                case 3:
                    selectFile(scanner);
                    break;
                default:
                    isExit = true;
            }
        }
    }

    private static void selectFile(Scanner scanner) throws JAXBException {
        Path path = fileDirectoryInput(scanner);
        Path fileName = fileNameInput(scanner);
        while (!Files.exists(path.resolve(fileName))) {
            System.out.println(convertToUTF8String(String.format("Файл %s не существует", fileName)));
            fileName = fileNameInput(scanner);
        }
        List.of(1, 2, 3).forEach(System.out::println);
        Path filePath = path.resolve(fileName);
        System.out.println(convertToUTF8String("Выберите действие"));
        System.out.println(convertToUTF8String("Записать в файл строку (1)"));
        System.out.println(convertToUTF8String("Вывести файл (2)"));
        System.out.println(convertToUTF8String("Удалить файл (3)"));
        System.out.println(convertToUTF8String("Записать в файл объект Person в формате JSON (4)"));
        System.out.println(convertToUTF8String("Десериализовать JSON файл (5)"));
        System.out.println(convertToUTF8String("Записать в файл объект Person в формате XML (6)"));
        System.out.println(convertToUTF8String("Десериализовать XML файл (7)"));
        System.out.println(convertToUTF8String("Архивировать файл в формате ZIP (8)"));
        int choice = inputChoice(scanner);
        switch (choice) {
            case 1:
                writeFile(filePath, scanner);
                break;
            case 2:
                printFile(filePath);
                break;
            case 3:
                deleteFile(filePath);
                break;
            case 4:
                writePersonToJsonFile(scanner, filePath);
                break;
            case 5:
                printJsonFile(filePath);
                break;
            case 6:
                writePersonToXmlFile(scanner, filePath);
                break;
            case 7:
                printXMLFile(filePath);
                break;
            case 8:
                archiveFile(filePath, scanner);
                break;
        }
    }

    private static void archiveFile(Path filePath, Scanner scanner) {
        Path path = filePath.getParent();
        String zipFileName = input(convertToUTF8String("Имя архива: "), scanner) + ".zip";
        Path zipFilePath = path.resolve(zipFileName);
        try {
            Files.createFile(path.resolve(zipFilePath));
        } catch (IOException e) {
            System.out.println(convertToUTF8String("Не удалось создать файл " + zipFilePath));
            return;
        }
        try (FileOutputStream zipOutputStream = new FileOutputStream(zipFilePath.toString());
             ZipOutputStream zipStream = new ZipOutputStream(zipOutputStream);
             FileInputStream fileInputStream = new FileInputStream(filePath.toString())) {
            ZipEntry zipEntry = new ZipEntry(filePath.getFileName().toString());
            zipStream.putNextEntry(zipEntry);
            byte[] bytes = fileInputStream.readAllBytes();
            zipStream.write(bytes);
        } catch (IOException e) {
            System.out.println(convertToUTF8String("Не удалось архивировать файл"));
        }
        System.out.println(convertToUTF8String("Файл успешно архивирован"));
    }

    private static void printXMLFile(Path filePath) throws JAXBException {
        if (!FilenameUtils.isExtension(filePath.toString(), "xml")) {
            System.out.println(convertToUTF8String("Файл не является XML файлом"));
            return;
        }
        JAXBContext context = JAXBContext.newInstance(Person.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Person person;
        try {
            person = (Person) unmarshaller.unmarshal(filePath.toFile());
        } catch (JAXBException e) {
            System.out.println(convertToUTF8String("Файл имеет неверную структуру"));
            return;
        }
        System.out.printf("FirstName: %s%n", person.getFirstName());
        System.out.printf("LastName: %s%n", person.getLastName());
        System.out.printf("MiddleName: %s%n", person.getMiddleName());
    }

    private static void printJsonFile(Path filePath) {
        if (!FilenameUtils.isExtension(filePath.toString(), "json")) {
            System.out.println(convertToUTF8String("Файл не является JSON файлом"));
            return;
        }
        List<Person> people;
        String stringFile = "";
        try {
            stringFile = FileUtils.readFileToString(filePath.toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println(convertToUTF8String("Не удалось прочитать файл"));
        }
        try {
            people = objectMapper.readValue(stringFile, new TypeReference<>(){});
        } catch (Exception e) {
            System.out.println(convertToUTF8String("JSON файл имеет неверную структуру"));
            return;
        }
        people.forEach(person -> {
            System.out.printf("FirstName: %s%n", person.getFirstName());
            System.out.printf("LastName: %s%n", person.getLastName());
            System.out.printf("MiddleName: %s%n", person.getMiddleName());
            System.out.println();
        });
    }

    private static void writePersonToXmlFile(Scanner scanner, Path filePath) throws JAXBException {
        if (!FilenameUtils.isExtension(filePath.toString(), "xml")) {
            System.out.println(convertToUTF8String("Файл не является XML файлом"));
            return;
        }
        String firstName = input("Имя: ", scanner);
        String lastName = input("Фамилия: ", scanner);
        String middleName = input("Отчество: ", scanner);
        Person person = new Person(firstName, lastName, middleName);
        JAXBContext context = JAXBContext.newInstance(Person.class);
        Marshaller mar = context.createMarshaller();
        mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        try {
            mar.marshal(person, filePath.toFile());
            System.out.println(convertToUTF8String("Объекты успешно записаны"));
        } catch (JAXBException exception) {
            System.out.println(convertToUTF8String("Не удалось записать данные в файл"));
        }
    }

    private static void writePersonToJsonFile(Scanner scanner, Path filePath) {
        if (!FilenameUtils.isExtension(filePath.toString(), "json")) {
            System.out.println(convertToUTF8String("Файл не является JSON файлом"));
            return;
        }
        List<Person> personList;
        try {
            personList = objectMapper.readValue(filePath.toFile(), new TypeReference<>(){});
        } catch (IOException e) {
            System.out.println(convertToUTF8String("Файл имеет неверную структуру"));
            return;
        }
        String firstName = input("Имя: ", scanner);
        String lastName = input("Фамилия: ", scanner);
        String middleName = input("Отчество: ", scanner);
        Person person = new Person(firstName, lastName, middleName);
        personList.add(person);
        try (FileWriter fileWriter = new FileWriter(filePath.toFile())) {
            String jsonPerson = objectMapper.writeValueAsString(personList);
            fileWriter.write(jsonPerson);
            System.out.println(convertToUTF8String("Объекты успешно записаны"));
        } catch (IOException exception) {
            System.out.println(convertToUTF8String("Не удалось записать данные в файл"));
        }
    }

    private static void deleteFile(Path filePath) {
        try {
            Files.delete(filePath);
            System.out.println(convertToUTF8String("Файл успешно удален"));
        } catch (IOException e) {
            System.out.println(convertToUTF8String("Не удалось удалить файл"));
        }
    }

    private static void writeFile(Path filePath, Scanner scanner) {
        String str = input("Строка: ", scanner);
        try (FileWriter fileWriter = new FileWriter(filePath.toFile())) {
            fileWriter.write(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(convertToUTF8String("Строка записана"));
    }

    private static void printFile(Path file) {
        try (BufferedReader in = new BufferedReader(new FileReader(file.toFile()))) {
            in.lines().forEach(line -> System.out.println(convertToUTF8String(line)));
        } catch (IOException exception) {
            System.out.println(convertToUTF8String("Не удалось прочитать файл"));
        }
    }

    private static void createFile(Scanner scanner) {
        try {
            Path path = fileDirectoryInput(scanner);
            Path fileName = fileNameInput(scanner);
            while (Files.exists(path.resolve(fileName))) {
                System.out.println(convertToUTF8String(String.format("Файл %s уже существует", fileName)));
                fileName = fileNameInput(scanner);
            }
            Files.createFile(path.resolve(fileName));
            System.out.println(convertToUTF8String("Файл создан"));
        } catch (IOException e) {
            System.out.println(convertToUTF8String("Произошла ошибка при создании файла"));
        }
    }

    private static Path fileDirectoryInput(Scanner scanner) {
        while (true) {
            String stringPath = input(String.format("Расположение файла (введите -, чтобы использовать путь по умолчанию, по умолчанию %s): ",
                    DEFAULT_FILE_PATH), scanner);
            Path path = stringPath.trim().equals("-") ? DEFAULT_FILE_PATH : Paths.get(stringPath);
            if (new File(path.toString()).isDirectory()) {
                return path;
            } else {
                System.out.println(convertToUTF8String(String.format("Путь %s не валиден", path)));
            }
        }
    }

    private static Path fileNameInput(Scanner scanner) {
        Pattern pattern = Pattern.compile("^(?!.*[<>:\"/\\\\|?*\\x00-\\x1F]).{1,255}$");
        while (true) {
            String fileNameStr = input("Имя файла: ", scanner);
            if (!pattern.matcher(fileNameStr).matches()) {
                System.out.println(convertToUTF8String(String.format("Имя файла %s не валидно", fileNameStr)));
            } else {
                return Paths.get(fileNameStr);
            }
        }
    }

    private static void printVolumesInfo() {
        File[] roots;
        FileSystemView fsv = FileSystemView.getFileSystemView();

        roots = File.listRoots();

        for(File root : roots)
        {
            System.out.println(convertToUTF8String("Название диска: ") + root);
            System.out.println(convertToUTF8String("Метка тома: ") + fsv.getSystemDisplayName(root));
            System.out.println(convertToUTF8String("Тип файловой системы: ") + getVolumeTypeByName(root.toString() + "\\"));
            System.out.println(convertToUTF8String("Размер диска: ") + root.getTotalSpace() / 1024 / 1024 + "mb");
            System.out.println();
        }
    }

    private static String convertToUTF8String(String str) {
        return new String(str.getBytes(), StandardCharsets.UTF_8);
    }

    private static void printOptions() {
        System.out.println(convertToUTF8String("Выберите действие"));
        System.out.println(convertToUTF8String("Создать файл (1)"));
        System.out.println(convertToUTF8String("Вывести информацию о дисках (2)"));
        System.out.println(convertToUTF8String("Выбрать файл (3)"));
        System.out.println(convertToUTF8String("Выйти (0)"));
    }

    private static String input(String invite, Scanner scanner) {
        System.out.print(convertToUTF8String(invite));
        return scanner.nextLine().trim();
    }

    private static int inputChoice(Scanner scanner) {
        int choice = 0;
        boolean isSuccess = false;
        while (!isSuccess) {
            System.out.print(convertToUTF8String(">> "));
            String input = scanner.nextLine();
            try {
                choice = Integer.parseInt(input.trim());
                isSuccess = true;
            } catch (NumberFormatException exception) {
                System.out.println(convertToUTF8String("Введите число"));
            }
        }
        return choice;
    }

    private native static String getVolumeTypeByName(String volumeName);
}