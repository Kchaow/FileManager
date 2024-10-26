import json
import zipfile
import psutil
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path

DEFAULT_FILE_PATH = Path.home()


@dataclass
class Person:
    first_name: str
    last_name: str
    middle_name: str


def select_file():
    path = file_directory_input()
    file_name = file_name_input()
    while not (path / file_name).exists():
        print(f"Файл {file_name} не существует")
        file_name = file_name_input()
    file_path = path / file_name
    print("Выберите действие")
    print("Записать в файл строку (1)")
    print("Вывести файл (2)")
    print("Удалить файл (3)")
    print("Записать в файл объект Person в формате JSON (4)")
    print("Десериализовать JSON файл (5)")
    print("Записать в файл объект Person в формате XML (6)")
    print("Десериализовать XML файл (7)")
    print("Архивировать файл в формате ZIP (8)")
    choice = input_choice()
    if choice == 1:
        write_file(file_path)
    elif choice == 2:
        print_file(file_path)
    elif choice == 3:
        delete_file(file_path)
    elif choice == 4:
        write_person_to_json_file(file_path)
    elif choice == 5:
        print_json_file(file_path)
    elif choice == 6:
        write_person_to_xml_file(file_path)
    elif choice == 7:
        print_xml_file(file_path)
    elif choice == 8:
        archive_file(file_path)


def archive_file(file_path: Path):
    path = file_path.parent
    zip_file_name = input("Имя архива: ") + ".zip"
    zip_file_path = path / zip_file_name
    zip_file_path.touch()
    zipfile.ZipFile(zip_file_path, 'w').write(file_path, file_path.name)
    print("Файл успешно архивирован")


def print_xml_file(file_path: Path):
    if file_path.suffix != ".xml":
        print("Файл не является XML файлом")
        return
    tree = ET.parse(file_path)
    root = tree.getroot()
    person = {child.tag: child.text for child in root}
    print(f"FirstName: {person.get('firstName')}")
    print(f"LastName: {person.get('lastName')}")
    print(f"MiddleName: {person.get('middleName')}")


def print_json_file(file_path: Path):
    if file_path.suffix != ".json":
        print("Файл не является JSON файлом")
        return
    with open(file_path, 'r', encoding='utf-8') as file:
        people = json.load(file)
        for person in people:
            print(f"FirstName: {person.get('firstName')}")
            print(f"LastName: {person.get('lastName')}")
            print(f"MiddleName: {person.get('middleName')}")
            print()


def write_person_to_xml_file(file_path: Path):
    if file_path.suffix != ".xml":
        print("Файл не является XML файлом")
        return
    first_name = input("Имя: ")
    last_name = input("Фамилия: ")
    middle_name = input("Отчество: ")
    person = ET.Element("Person")
    ET.SubElement(person, "firstName").text = first_name
    ET.SubElement(person, "lastName").text = last_name
    ET.SubElement(person, "middleName").text = middle_name
    tree = ET.ElementTree(person)
    tree.write(file_path, encoding='utf-8', xml_declaration=True)
    print("Объекты успешно записаны")


def write_person_to_json_file(file_path: Path):
    if file_path.suffix != ".json":
        print("Файл не является JSON файлом")
        return
    with open(file_path, 'r', encoding='utf-8') as file:
        person_list = json.load(file)
    first_name = input("Имя: ")
    last_name = input("Фамилия: ")
    middle_name = input("Отчество: ")
    person = {"firstName": first_name, "lastName": last_name, "middleName": middle_name}
    person_list.append(person)
    with open(file_path, 'w', encoding='utf-8') as file:
        json.dump(person_list, file, ensure_ascii=False, indent=4)
        print("Объекты успешно записаны")


def delete_file(file_path: Path):
    file_path.unlink()
    print("Файл успешно удален")


def write_file(file_path: Path):
    str_input = input("Строка: ")
    open(file_path, 'w', encoding='utf-8').write(str_input)
    print("Строка записана")


def print_file(file_path: Path):
    with open(file_path, 'r', encoding='utf-8') as file:
        for line in file:
            print(line.strip())


def create_file():
    path = file_directory_input()
    file_name = file_name_input()
    while (path / file_name).exists():
        print(f"Файл {file_name} уже существует")
        file_name = file_name_input()
    (path / file_name).touch()
    print("Файл создан")


def file_directory_input() -> Path:
    while True:
        string_path = input(f"Расположение файла (введите -, чтобы использовать путь по умолчанию, по умолчанию {DEFAULT_FILE_PATH}): ").strip()
        path = DEFAULT_FILE_PATH if string_path == "-" else Path(string_path)
        if path.is_dir():
            return path
        else:
            print(f"Путь {path} не валиден")


def file_name_input() -> str:
    import re
    pattern = re.compile(r"^(?!.*[<>:\"/\\|?*\x00-\x1F]).{1,255}$")
    while True:
        file_name_str = input("Имя файла: ").strip()
        if not pattern.match(file_name_str):
            print(f"Имя файла {file_name_str} не валидно")
        else:
            return file_name_str


def print_volumes_info():
    partitions = psutil.disk_partitions()
    for partition in partitions:
        print(f"Название диска: {partition.device}")
        print(f"Метка тома: {partition.mountpoint}")
        print(f"Тип файловой системы: {partition.fstype}")
        try:
            usage = psutil.disk_usage(partition.mountpoint)
            print(f"Размер диска: {usage.total // (1024 * 1024)}mb")
        except:
            print("Размер диска: неизвестен")
        print()


def print_options():
    print("Выберите действие")
    print("Создать файл (1)")
    print("Вывести информацию о дисках (2)")
    print("Выбрать файл (3)")
    print("Выйти (0)")


def input_choice() -> int:
    while True:
        try:
            choice = int(input(">> ").strip())
            return choice
        except ValueError:
            print("Введите число")


while True:
    print_options()
    choice = input_choice()
    if choice == 1:
        create_file()
    elif choice == 2:
        print_volumes_info()
    elif choice == 3:
        select_file()