import urllib.request  # for connecting and getting html content
from bs4 import BeautifulSoup  # for parsing html
from time import sleep
import file_parser
from classes import College, HighSchool
from mongoengine import *
connect('account', host='localhost', port=27017)

# Scraping Algorithm:
# 1. Get the HTML text, make it pretty.
# 2. Locate the important section.
# 3. Parse the important section for info.


# Function to get the soupified html content of url
def get_pretty_html(url):
    # header from a webscraping tutorial, to fool server
    hdr = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
           'AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 '
           'Safari/537.36',
           'Accept': 'text/html,application/xhtml+xml,application/xml;'
           'q=0.9,*/*;q=0.8',
           'Accept-Charset': 'ISO-8859-1,utf-8;q=0.7,*;q=0.3',
           'Accept-Language': 'en-US,en;q=0.8'}

    # actually get the site's content
    req = urllib.request.Request(url, headers=hdr)
    html = urllib.request.urlopen(req)

    # make the content pretty
    soup = BeautifulSoup(html, 'html.parser')
    return soup


def get_college_ranking():
    WSJ_THE_URL = "http://allv22.all.cs.stonybrook.edu/~stoller/"\
                  "cse416/WSJ_THE/index.html"

    # get the parsed html
    soup = get_pretty_html(WSJ_THE_URL)

    d = soup.find("table", {"id": "datatable-1"})
    d = soup.find('table').find_all('tr')

    data = []
    for td in d:
        row = []
        for tr in td.strings:
            row.append(tr)
        data.append(row)
    # print(data[0:10])

    cleanedData = []
    for line in data:
        line[0] = line[0].strip(' ><=')  # ranking, remove the =7, >600, etc
        cleanedData.append(line)

    rank_to_names = {}
    name_to_rank = {}
    for line in cleanedData[1:]:  # ignore the header
        rank = line[0]
        name = line[1]
        if rank not in rank_to_names:
            rank_to_names[rank] = []
        rank_to_names[rank].append(name)
        name_to_rank[name] = rank
    return {'rank to name': rank_to_names, 'name_to_rank': name_to_rank}


def update_college_ranking():
    data = get_college_ranking()['name_to_rank']
    colleges = file_parser.get_collegetxt_list()
    for name in colleges:
        if name in data:
            try:
                ranking = data[name]
                if '-' in ranking:  # e.g. 401-500 => 500
                    ranking = ranking.split('-')[1]
                c = College.objects.get(name=name)
                c.ranking = int(ranking)
                c.save()
            except Exception as e:
                print(e)
                try:  # college not in db, so make a class for it
                    c = College(name=name, ranking=int(ranking))
                    c.save()
                except:
                    print("Error updating rank for college: "+name)
                    continue


def get_college_data_data(name):
    url_name = college_name_conversion(name)
    url = 'http://allv22.all.cs.stonybrook.edu/~stoller/cse416/collegedata/'
    # url = 'https://www.niche.com/k12/'
    # url = 'https://www.collegedata.com/college/'
    url = url + url_name
    print(url)
    majors = []
    fresh_grades = []
    pop_stats = []
    cost = []

    soup = get_pretty_html(url)

    # undergrad majors:
    # <h3 class="h5">Undergraduate Majors</h3>
    data = soup.find("h3", string="Undergraduate Majors")
    data = data.next_sibling.next_sibling  # list of majors is 2 siblings down
    majors = [major.strip() for major in data.strings if major.strip() != '']

    # Qualifications of Enrolled Freshmen
    # <h3 class="h5">Qualifications of Enrolled Freshmen</h3>
    data = soup.find("h3", string="Qualifications of Enrolled Freshmen")
    data = data.next_sibling.next_sibling
    grades = [grade.strip() for grade in data.strings if grade.strip() != '']
    fresh_grades = grades
    avg_gpa_index = fresh_grades.index('Average GPA')+1
    # the below are supposed to be average index, may not exist...
    sat_math_index = fresh_grades.index('SAT Math')+1
    sat_ebrw_index = fresh_grades.index('SAT EBRW')+1
    act_comp_index = fresh_grades.index('ACT Composite')+1
    avg_gpa = None
    if (fresh_grades[avg_gpa_index] != 'Not reported'):
        avg_gpa = float(fresh_grades[avg_gpa_index])

    # in case there are no values at the site
    avg_sat_math, sat_math_25th, sat_math_75th = None, None, None
    if ('average' in fresh_grades[sat_math_index]):
        # then we know there's a provided average, use it
        avg_sat_math = int(fresh_grades[sat_math_index].split()[0])
        sat_math_index += 1  # move the pointer along, to the next info
    if ('range of middle 50%' in fresh_grades[sat_math_index]):
        # '590-680 range of middle 50%'
        d = fresh_grades[sat_math_index].split()[0].split('-')
        sat_math_25th = int(d[0])
        sat_math_75th = int(d[1])
        if avg_sat_math is None:
            avg_sat_math = (sat_math_25th+sat_math_75th)/2
    avg_sat_ebrw, sat_ebrw_25th, sat_ebrw_75th = None, None, None
    if ('average' in fresh_grades[sat_ebrw_index]):
        avg_sat_ebrw = int(fresh_grades[sat_ebrw_index].split()[0])
        sat_ebrw_index += 1
    if ('range of middle 50%' in fresh_grades[sat_ebrw_index]):
        d = fresh_grades[sat_ebrw_index].split()[0].split('-')
        sat_ebrw_25th = int(d[0])
        sat_ebrw_75th = int(d[1])
        if avg_sat_ebrw is None:
            avg_sat_ebrw = (sat_ebrw_25th+sat_ebrw_75th)/2
    avg_act_comp, act_comp_25th, act_comp_75th = None, None, None
    if ('average' in fresh_grades[act_comp_index]):
        avg_act_comp = int(fresh_grades[act_comp_index].split()[0])
        act_comp_index += 1
    if ('range of middle 50%' in fresh_grades[act_comp_index]):
        d = fresh_grades[act_comp_index].split()[0].split('-')
        act_comp_25th = int(d[0])
        act_comp_75th = int(d[1])
        if avg_act_comp is None:
            avg_act_comp = (act_comp_25th+act_comp_75th)/2

    # Cost of School
    # based on the former seeker, because cost is right after grades
    for x in range(0, 3):
        data = data.next_sibling
    for x in range(0, 3):
        data = data.next_element
    cost = [d.strip() for d in data.strings if d is not None]
    cost = [d for d in cost if d != '']
    index = cost.index('Cost of Attendance')+1
    in_state, out_state = None, None
    if ('In-state:' in cost[index]):
        # 'In-state: $26,091', 'Out-of-state: $43,761'
        in_state = cost[index].split()[1].replace(',', '')[1:]
        index += 1
    if ('Out-of-state:' in cost[index]):
        out_state = cost[index].split()[1].replace(',', '')[1:]
    if ('Out-of-state:' not in cost[index] and
            'In-state:' not in cost[index] and
            cost[index] != 'Not available' and
            in_state is None and out_state is None):
        # then this means that this is a private university, one cost
        in_state = cost[index].strip().replace(',', '')[1:]
        out_state = cost[index].strip().replace(',', '')[1:]
    # Student Population Statss:
    # <div class="statbar">
    data = soup.find("div", {"class": "statbar"})
    pop_stats = [stat.strip() for stat in data.strings if stat.strip() != '']
    # pop_stats not used...

    data = soup.find("h2", string="Undergraduate Retention and Graduation")
    data = data.next_sibling.next_sibling.next_sibling.next_sibling
    survival_stats = [d.strip() for d in data.strings if d.strip() != '']
    # we only want completeion rate within 4 years from this:
    index = survival_stats.index('Students Graduating Within 4 Years')+1
    completion_rate = None
    if (survival_stats[index] != 'Not reported'):
        completion_rate = float(survival_stats[index].strip().replace('%', ''))

    return {'avg gpa': avg_gpa, 'avg sat math': avg_sat_math,
            '25th sat math': sat_math_25th, '75th sat math': sat_math_75th,
            'avg sat ebrw': avg_sat_ebrw, '25th sat ebrw': sat_ebrw_25th,
            '75th sat ebrw': sat_ebrw_75th, 'avg act': avg_act_comp,
            '25th act': act_comp_25th, '75th act': act_comp_75th,
            'in cost': in_state, 'out cost': out_state,
            'majors': majors, 'completion_rate': completion_rate}
    return {'majors': majors, 'freshman grades': fresh_grades, 'cost': cost,
            'pop_stats': pop_stats}


def update_college_data_data(name):
    # clean for db class storage purposes
    data = get_college_data_data(name)
    colleges = file_parser.get_collegetxt_list()
    print(name)

    try:
        c = College.objects.get(name=name)
    except Exception as e:
        print(e)
        print("College Not in DB: "+name)
        c = College(name=name)
        c.save()

    c.completion_rate = data['completion_rate']
    c.majors = data['majors']
    c.avg_gpa = data['avg gpa']
    c.avg_sat_math = data['avg sat math']
    c.sat_math_25 = data['25th sat math']
    c.sat_math_75 = data['75th sat math']
    c.avg_sat_ebrw = data['avg sat ebrw']
    c.sat_ebrw_25 = data['25th sat ebrw']
    c.sat_ebrw_75 = data['75th sat ebrw']
    c.avg_act_composite = data['avg act']
    c.act_composite_25 = data['25th act']
    c.act_composite_75 = data['75th act']
    c.in_cost = data['in cost']
    c.out_cost = data['out cost']
    c.save()


def college_name_conversion(name):
    # TAILORED FOR COLLEGEDATA.COM ONLY

    # Remove leading "The"
    if (name[:4] == "The "):
        name = name[4:]
    # Replace commas from location:
    # Example "University, East Bay" -> "University Easy Bay"
    name = name.replace(',', '')
    # Remove amperstamps
    name = name.replace(' & ', ' ')
    # Replace SUNY >:(
    name = name.replace('SUNY', 'State University of New York')
    # Replace spaces with dashes
    name = name.replace(' ', '-')
    return name


def update_all_colleges():
    colleges = file_parser.get_collegetxt_list()
    for name in colleges:
        if name == '':
            continue
        # name = clean_college_name(name)
        # print(name)
        update_college_data_data(name)


def get_highschool_info(name, city, state):
    # name = name.replace('.', '')  # remove dots, like Mt. Brook
    name = name.replace(' ', '-')+'-'+city.replace(' ', '-')+'-'+state+'/'
    name = name.lower()
    # url = 'https://www.niche.com/k12/'+name
    url = 'http://allv22.all.cs.stonybrook.edu/~stoller/cse416/niche/'+name
    print(url)
    # url = 'http://allv22.all.cs.stonybrook.edu/~stoller/cse416/niche/' \
    #    'academic-magnet-high-school-north-charleston-sc/'
    soup = get_pretty_html(url)

    data = soup.find_all("div", {"class": "profile__bucket--1"})
    data = data[5]  # 5th one in the list with this class in div...
    percent_reading = [x for x in data.strings][2]
    data = data.next_sibling
    percent_math = [x for x in data.strings][2]
    data = data.next_sibling
    data = [x for x in data.strings]
    grad_rate = data[2]
    avg_sat = data[5]
    avg_act = data[11]
    ap_enroll = data[16]
    return {'reading': percent_reading, 'math': percent_math,
            'grad rate': grad_rate, 'sat': avg_sat, 'act': avg_act,
            'ap': ap_enroll}


def highschool_exists(name, city, state):
    existed = False
    try:
        c = HighSchool.objects.get(name=name, city=city, state=state)
        # HS in DB, and is scraped, we're done
        if (c.scraped == True):
            return True
        # Else HS not scraped, need to wait
        existed = True
        sleep(1)  # wait for the scraping to finish, get new data
        c = HighSchool.objects.get(name=name, city=city, state=state)
        if (c.scraped == True):
            print("DONE SCRAPING, WORKS!")
            return True
        return False  # the scraping we waited for failed, 
    except Exception as e:
        print(e)
        if existed:  # there was data that was no longer there
            print("Disappeared!")
            return False  # must be bad info to scrape
        print("WE'RE GONNA SCRAPE!")
        # reaching here means HS not in db, so add info
        try:
            print("Saving, gonna be first")
            c = HighSchool(name=name, city=city, state=state, scraped=False)
            c.save()  # the atomic action? not really...
            try:
                c = HighSchool.objects.get(name=name, city=city, state=state, scraped=False)
            except Exception as e: # got sniped, someone saved first
                c.delete() # there was a problem, 2 found
                print("Blocked on entrace")
                print(e)  # let them scrape first, we go back to the start and wait
                return highschool_exists(name, city, state)
        # duplicate exception, wasn't called during testing, but just in case
        except Exception as e:  # got sniped, someone saved first
            print("Blocked #3 on entrance")
            print(e)  # let them scrape first, we go back to the start and wait
            return highschool_exists(name, city, state)
        # we've staked our claim, we're first, now scrape!
        try:
            print("STARTING SCRAPING")
            success = update_highschool_data(name, city, state)
            if not success:  # failed scraping
                c.delete()
                print("SCRAPING FAILED>>>>")
                return False  # failed
            return True
        except Exception as e:
            print(e)
            print("Last error")
            try:
                c.delete()  # delete what we added
            except:
                return False
            return False

def update_highschool_data(name, city, state):
    '''  # new function purpose, would rather crash for atomicity
    try:
        c = HighSchool.objects.get(name=name, city=city, state=state)
    except Exception as e:  # should never happend, but in case
        print(e)
        print("High School Not in DB: "+name)
        c = HighSchool(name=name, city=city, state=state, scraped=False)
        # c.save()
    '''
    c = HighSchool.objects.get(name=name, city=city, state=state) 
    try:
        data = get_highschool_info(name, city, state)
    except Exception as e:
        print(e)
        return False  # can't get college info
    # [:-1] is to remove percent sign in string
    try:
        c.reading_prof = int(data["reading"][:-1])
    except:
        pass
    try:
        c.math_prof = int(data["math"][:-1])
    except:
        pass
    try:
        c.grad_rate = int(data["grad rate"][:-1])
    except:
        pass
    try:
        c.avg_sat = int(data["sat"])
    except:
        pass
    try:
        c.avg_act = int(data["act"])
    except:
        pass
    try:
        c.ap_enroll = int(data["ap"][:-1])
    except:
        pass
    c.scraped = True
    c.save()
    return True


# tests:
# print(get_college_ranking())
# print(get_college_data_data("Stony Brook University"))
# update_college_data_data("Stony Brook University")
# update_college_ranking()
# print(get_highschool_info("Stuyvesant High School", 'New York', "NY"))
# print(update_highschool_data("Stuyvesant High School", 'New York', "NY"))
# print(highschool_exists("Stuyvesant High School1", 'New York', "NY"))
# print(update_highschool_data('Academic Magnet High School', 'North Charleston', 'SC'))
# print(update_highschool_data('Ward Melville Senior High School', 'East Setauket', 'NY'))
# update_all_colleges()
