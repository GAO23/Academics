import csv
import hash_utils
import requests
from classes import Account, Application, StudentProfile, College
# from scraper import highschool_exists  # cannot be here because this is imported into scraper
from algorithms import detect_questionable_acceptance
from time import time
import threading
from mongoengine import *
connect('account', host='localhost', port=27017)


def import_student_data(filename):
    lines = []
    with open(filename) as f:
        reader = csv.reader(f)
        for line in reader:
            lines.append(line)

    header = lines[0]
    threads = []
    max = 200  # this is lighter
    lines = lines[1:]
    for x in range(0, max):
        start = int(len(lines)/max * x)
        end = int(len(lines)/max * (x+1))
        print(start, end)
        subset = lines[start:end]
        t = threading.Thread(target=thread_import_student_data, args=(subset,))
        threads.append(t)
        # threads.append(target=thread_import_student_data, args=(lines[start:end]))
        threads[-1].start()
    for x in threads:
        x.join()
    return


def thread_import_student_data(lines):
    from scraper import highschool_exists
    index = ['username', 'password', 'state', 'high_school_name',
             'high_school_city', 'high_school_state', 'gpa', 'college_class',
             'major_1', 'major_2', 'sat_math', 'sat_ebrw', 'act_english',
             'act_math', 'act_reading', 'act_science', 'act_composite',
             'sat_lit', 'sat_us', 'sat_world', 'sat_math_1', 'sat_math_2',
             'sat_eco_bio', 'sat_mol_bio', 'sat_chem', 'sat_physics',
             'ap_passed']

    for line in lines:
        username = line[0]
        password = line[1]
        salt = hash_utils.generate_salt()
        digest = hash_utils.hmac_hash(password, salt)
        account = Account(username=username,
                          hashed_password=digest,
                          salt=salt, type="Student")
        try:
            account.save()
        except Exception as e:
            # print(e)
            print("There was an error importing student data: "+username)

    for line in lines:
        username = line[0]
        try:
            account = Account.objects.get(username=username, type="Student")
        except:
            print("Account could not be loaded: "+username)
            continue
        # Make student profile class
        # TODO: Decide on what attributes are optional
        # TODO: Add checks for if this data is not pressent?
        hs_name = line[index.index('high_school_name')].title().replace(".", "")
        hs_city = line[index.index('high_school_city')].title()
        hs_state = line[index.index('high_school_state')]
        if hs_name != '' and hs_city != '' and hs_state != '':
            highschool_exists(hs_name, hs_city, hs_state)
        p = StudentProfile(
            student=account,
            gpa=float(line[index.index('gpa')]),
            residence_state=line[index.index('state')],
            high_school_name=hs_name,
            high_school_city=hs_city,
            high_school_state=hs_state,
            college_class=int(line[index.index('college_class')]))
        for x in range(index.index('major_1'), len(index)):
            info = index[x]
            data = line[x]
            if data != '':  # the field isn't empty
                if info == "major_1":
                    p.major_1 = data
                elif info == "major_2":
                    p.major_2 = data
                else:
                    p.grades[info] = int(data)
        try:
            p.save()
        except Exception as e:
            print(e)
            print("Profile couldn't be loaded: "+username)
            continue


def import_application_data(filename):
    lines = []
    with open(filename) as f:
        reader = csv.reader(f)
        for line in reader:
            lines.append(line)

    lines = lines[1:]  # ignore header
    max = 80
    threads = []
    for x in range(0, max):
        start = int(len(lines)/max * x)
        end = int(len(lines)/max * (x+1))
        print(start, end)
        subset = lines[start:end]
        t = threading.Thread(target=thread_import_application_data, args=(subset,))
        threads.append(t)
        threads[-1].start()
    for x in threads:
        x.join()
    return


def thread_import_application_data(lines):
    for line in lines:
        username = line[0]
        college = line[1].strip()
        status = line[2].capitalize()
        # find the student by username
        # find the college by name

        try:
            account = Account.objects.get(username=username, type="Student")
            student = StudentProfile.objects.get(student=account)
        except DoesNotExist as e:
            print("Student Doesn't Exist: "+username)
            continue
        try:
            university = College.objects.get(name=college)
        except Exception as e:  # DoesNotExist as e:
            print(e)
            print("College Doesn't Exist: "+college)
            continue

        verification = "Approved"
        if (status == "Accepted" and 
            detect_questionable_acceptance(university, student) < 50):
            verification = "Pending"

        duplicate = False
        try:
            app = Application.objects.get(student=student,college=university)
            print("Application already exists, moving on")
            continue
        except:
            duplicate = False
        ID = hash_utils.sha_hash(username+"+=+"+college)
        app = Application(ID=ID, student=student,
                          college=university,
                          status=status,
                          verification=verification,
                          timestamp=str(time()))
        try:
            app.save()
        except Exception as e:
            print("There was a problem with this application")
            print(e)
            print(line[0]+" "+line[1])
            continue


def delete_student_data():
    Account.objects(type="Student").delete()
    # StudentProfile.objects().delete()
    # Application.objects().delete()


def institution_type(arg):
    switcher = {
        "1": "Public",
        "2": "Private Nonprofit",
        "3": "Private For-Profit",
    }
    return switcher.get(arg, None)


def get_region(region, state):
    northeast = ["NJ", "NY", "PA"]
    south = ["DE", "DC", "MD", "OK", "TX"]
    west = ["AZ", "NM"]
    if region == "1" or state in northeast:
        return "Northeast"
    if region == "3" or region == "4":
        return "Midwest"
    if region == "5" or state in south:
        return "South"
    if region == "7" or region == "8" or state in west:
        return "West"
    if region == "9":
        return "Other"


def import_college_scorecard():
    f = open('colleges.txt', "r")
    college_list = []
    mod_list = []
    for c in f:
        n = c.rstrip()
        college_list.append(n)
        mod_list.append(
            n.replace("&", "and").replace(",", "").replace("The ", "")
            )
    f.close()
    url = 'https://ed-public-download.app.cloud.gov/downloads/Most-Recent-Cohorts-All-Data-Elements.csv'
    sc = requests.get(url).text.splitlines()
    sc_reader = csv.reader(sc)
    header = next(sc_reader)
    for line in sc_reader:
        sc_name = line[header.index("INSTNM")]
        sc_mod_name = sc_name.replace("-", " ")
        sc_mod_name = sc_mod_name.replace("The ", "")
        sc_mod_name = sc_mod_name.replace("Saint", "St")
        name = ""
        if sc_name in college_list:
            name = sc_name
        elif sc_mod_name in mod_list:
            name = college_list[mod_list.index(sc_mod_name)]
        if name != "":
            city = line[header.index("CITY")]
            state = line[header.index("STABBR")]
            region = get_region(line[header.index("REGION")], state)
            institution = institution_type(line[header.index("CONTROL")])
            adm_rate = line[header.index("ADM_RATE")]
            if adm_rate != "NULL":
                admission_rate = round(float(adm_rate)*100, 2)
            else:
                admission_rate = adm_rate
            size = int(line[header.index("UGDS")])
            median_debt = line[header.index("GRAD_DEBT_MDN")]
            salary = line[header.index("MN_EARN_WNE_P6")]
            try:
                college = College.objects.get(name=name)
                college.update(set__city=city)
                college.update(set__state=state)
                college.update(set__region=region)
                college.update(set__institution=institution)
                if admission_rate != "NULL":
                    college.update(set__admission_rate=admission_rate)
                college.size = size
                college.save()
                college.update(set__median_debt=median_debt)
                college.update(set__salary=salary)
            except Exception as e:
                print(e)
                if admission_rate != "NULL":
                    college = College(
                        name=name, city=city, state=state,
                        region=region, institution=institution,
                        admission_rate=admission_rate, size=size,
                        median_debt=median_debt, salary=salary,
                    )
                else:
                    college = College(
                        name=name, city=city, state=state, region=region,
                        institution=institution, size=size,
                        median_debt=median_debt, salary=salary,
                    )
                # try:
                college.save()
                # except:
                #    print("Error importing college: " + name)

# global variable to store the file content of colleges.txt in list form
college_list = None


def generate_collegetxt_list():
    global college_list
    with open('colleges.txt') as f:
        data = f.read().split('\n')
        cleaned_data = []
        for college in data:  # clean the names
            name = college  # moved cleaning elsewhere
            if (name.strip() == ''):
                continue  # no empty string as name
            cleaned_data.append(name)
    college_list = cleaned_data
    return cleaned_data


def get_collegetxt_list(refresh=False):
    global college_list
    if (college_list is None or refresh is False):
        return generate_collegetxt_list()
    return college_list

'''
college = College(name='Massachusetts Institute of Technology')
college.save()
college = College(name='Stony Brook University')
college.save()

college = College(name='Princeton')
college.save()
college = College(name='Cornell University')
college.save()'''
# delete_student_data()
# import_student_data("students-1.csv")
# import_application_data('applications-1.csv')
