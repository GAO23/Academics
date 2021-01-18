from script import calc_academic_similarity as CAS
def estimate_exam_percentile(minimum, first_quartile, third_quartile,
                             maximum, score):
    if score <= first_quartile:
        step = (first_quartile-minimum)/25
        return int((score-minimum)/step)
    elif score <= third_quartile:
        step = (third_quartile-first_quartile)/50
        return int(25+(score-first_quartile)/step)
    else:
        step = (maximum-third_quartile)/25
        return int(75+(score-third_quartile)/step)


def estimate_gpa_percentile(minimum, mean, gpa):
    print(minimum, mean, gpa)
    if gpa < minimum:
        return 0
    elif gpa < mean:
        if mean < 4:
            step = (mean-minimum)/50
        else:
            step = (mean-minimum)/100
        return int((gpa-minimum)/step)
    else:
        if mean < 4:
            step = (4.0-mean)/50
        else:
            return 100
        return int(50+(gpa-mean)/step)


def detect_questionable_acceptance(college, student):
    SAT_MIN = 200
    SAT_MAX = 800
    ACT_MIN = 2
    ACT_MAX = 36

    grades = student.grades
    # Estimate percentile for SAT Math
    sat_math_percentile = None
    if 'sat_math' in grades and grades["sat_math"] not in {None, ""}:
        student_sat_math = grades["sat_math"]
        sat_math_25 = college.sat_math_25
        sat_math_75 = college.sat_math_75
        if sat_math_25 and sat_math_75:
            sat_math_percentile = estimate_exam_percentile(SAT_MIN,
                                                           sat_math_25,
                                                           sat_math_75,
                                                           SAT_MAX,
                                                           int(student_sat_math))
            # Mark as questionable if below 1st percentile
            if sat_math_percentile < 1:
                return 0

    # Estimate percentile for SAT EBRW
    sat_ebrw_percentile = None
    if 'sat_ebrw' in grades and grades["sat_ebrw"] not in {None, ""}:
        student_sat_ebrw = grades["sat_ebrw"]
        sat_ebrw_25 = college.sat_ebrw_25
        sat_ebrw_75 = college.sat_ebrw_75
        if sat_ebrw_25 and sat_ebrw_75:
            sat_ebrw_percentile = estimate_exam_percentile(SAT_MIN,
                                                           sat_ebrw_25,
                                                           sat_ebrw_75,
                                                           SAT_MAX,
                                                           int(student_sat_ebrw))
            # Mark as questionable if below 1st percentile
            if sat_ebrw_percentile < 1:
                return 0

    # Estimate percentile for ACT Composite
    act_percentile = None
    if 'act_composite' in grades and grades["act_composite"] not in {None, ""}:
        student_act = grades["act_composite"]
        act_composite_25 = college.act_composite_25
        act_composite_75 = college.act_composite_75
        if act_composite_25 and act_composite_75:
            act_percentile = estimate_exam_percentile(ACT_MIN,
                                                      act_composite_25,
                                                      act_composite_75,
                                                      ACT_MAX,
                                                      int(student_act))
            # Mark as questionable if below 1st percentile
            if act_percentile < 1:
                return 0

    # Estimate percentile for GPA
    gpa_percentile = None
    student_gpa = student.gpa
    if student_gpa is not None:
        avg_gpa = college.avg_gpa
        if avg_gpa is not None:
            gpa_percentile = estimate_gpa_percentile(avg_gpa*0.8,
                                                     avg_gpa,
                                                     float(student_gpa))

    # Calculate final score
    score = 0
    denominator = 0
    if sat_math_percentile:
        score += sat_math_percentile*0.2
        denominator += 0.2
    if sat_ebrw_percentile:
        score += sat_ebrw_percentile*0.2
        denominator += 0.2
    if act_percentile:
        score += act_percentile*0.4
        denominator += 0.4
    if gpa_percentile:
        score += gpa_percentile*0.2
        denominator += 0.2
    if denominator != 0:
        return score/denominator
    return -1


def calc_academic_similarity(college, student):
    return CAS(college, student)


def compute_recommendation_score(college, student):
    # Calculate acceptance likelihood aspect
    acceptance_likelihood = detect_questionable_acceptance(college, student)
    # Calculate academic similarity aspect
    academic_similarity = calc_academic_similarity(college, student)

    # Calculate non-academic similarity aspect
    non_academic_factors = []
    debt = college.median_debt
    if debt not in {None, "NULL"}:
        non_academic_factors.append(1-int(debt)/100000)
    if student.residence_state == college.state:
        cost = college.in_cost
    else:
        cost = college.out_cost
    if cost is not None:
        non_academic_factors.append(1-int(cost)/40000)
    salary = college.salary
    if salary not in {None, "NULL"}:
        non_academic_factors.append(int(salary)/50000)
    completion_rate = college.completion_rate
    if completion_rate is not None:
        non_academic_factors.append(float(completion_rate/100))
    non_academic_suitability = 0
    if non_academic_factors != []:
        non_academic_suitability = max(2, sum(non_academic_factors) /
                                       len(non_academic_factors))*100
    
    print(acceptance_likelihood, academic_similarity, non_academic_suitability)
    # Calculate final score
    score = 0
    denominator = 0
    if acceptance_likelihood >= 0:
        score += acceptance_likelihood*0.4
        denominator += 0.4
    if academic_similarity >= 0:
        score += academic_similarity*0.4
        denominator += 0.4
    if non_academic_suitability > 0:
        score += non_academic_suitability*0.2
        denominator += 0.2
    ranking = (600 - college.ranking) / 6 if college.ranking is not None else 0.0
    if denominator != 0:
        score = score/denominator
        score += ranking
        score = score / .250
        return round(score, 2)
    return 0


def compare_highschool_grades(h1, h2):
    # function compares the students according for high school similarity algo
    data = 6
    diff = 0
    if (h1.reading_prof is not None and h2.reading_prof is not None):
        diff = (h1.reading_prof - h2.reading_prof)**2
        data -= 1
    if (h1.math_prof is not None and h2.math_prof is not None):
        diff += (h1.math_prof - h2.math_prof)**2
        data -= 1
    if (h1.grad_rate is not None and h2.grad_rate is not None):
        diff += (h1.grad_rate - h2.grad_rate)**2
        data -=	1
    if (h1.ap_enroll is not None and h2.ap_enroll is not None):
        diff += (h1.ap_enroll - h2.ap_enroll)**2
        data -=	1
    if (h1.avg_sat is not None and h2.avg_sat is not None):
        diff += ((h1.avg_sat - h2.avg_sat)*100/1600.)**2
        data -=	1
    if (h1.avg_act is not None and h2.avg_act is not None):
        diff += ((h1.avg_act - h2.avg_act)*100/36)**2
        data -=	1
    if data > 0:
        diff = diff / ((6.0-data)/6.0) if data != 6 else None
    return diff


def compare_students(s1, s2):
    # function compares the students according for high school similarity algo
    # s1 and s2 are both lists students
    s1_gpa = [s.gpa for s in s1 if s.gpa is not None]
    s2_gpa = [s.gpa for s in s2 if s.gpa is not None]
    s1_avg_gpa, s2_avg_gpa, gpa_num = None, None, None
    if s1_gpa != [] and s2_gpa != []:
        s1_avg_gpa = sum(s1_gpa)/1.0/len(s1_gpa) if len(s1_gpa) != 0 else None
        s2_avg_gpa = sum(s2_gpa)/1.0/len(s2_gpa) if len(s2_gpa) != 0 else None
        gpa_num = min(len(s1_gpa), len(s2_gpa))

    s1_sat = []
    for s in s1:
        if s.grades is not None:
            grade, numbers = 0, 0
            if 'sat math' in s.grades:
                grade += int(s.grades['sat math'])
                numbers += 1
            if 'sat ebrw' in s.grades:
                grade += int(s.grades['sat ebrw'])
                numbers += 1
            grade = grade*2 if numbers == 1 else grade
            s1_sat.append(grade)
    s2_sat = []
    for s in s2:
        if s.grades is not None:
            grade, numbers = 0,	0
            if 'sat math' in s.grades:
                grade += int(s.grades['sat math'])
                numbers += 1
            if 'sat ebrw' in s.grades:
                grade += int(s.grades['sat ebrw'])
                numbers += 1
            grade = grade*2 if numbers == 1 else grade
            s2_sat.append(grade)

    # s1_sat = [int(s.sat) for s in s1 if s.grades!=None and 'sat' in s.grades]
    # s2_sat = [s.sat for s in s2 if s.grades!=None and 'sat' in s.grades]
    s1_avg_sat, s2_avg_sat, sat_num = None, None, None
    if s1_sat != [] and s2_sat != []:
        s1_avg_sat = sum(s1_sat) / 1.0 / len(s1_sat)
        s2_avg_sat = sum(s2_sat) / 1.0 / len(s2_sat)
        sat_num = min(len(s1_sat), len(s2_sat))

    s1_act = []
    s2_act = []
    for s in s1:
        if s.grades is not None and 'act composition' in s.grades:
            s1_act.append(int(s.grades["act composition"]))
    for s in s2:
        if s.grades is not None and 'act composition' in s.grades:
            s2_act.append(int(s.grades["act composition"]))

    s1_avg_act, s2_avg_act, act_num = None, None, None
    if s1_act != [] and s2_act != []:
        s1_avg_act = sum(s1_act) / 1.0 / len(s1_act)
        s2_avg_act = sum(s2_act) / 1.0 / len(s2_act)
        act_num = min(len(s1_act), len(s2_act))

    print(gpa_num, sat_num, act_num)
    gpa_weight, sat_weight, act_weight = None, None, None
    if gpa_num is not None:
        gpa_weight = ((s1_avg_gpa - s2_avg_gpa) * 100/4.0 * gpa_num / 100)**2
    if sat_num is not None:
        sat_weight = ((s1_avg_sat - s2_avg_sat) * 100/1600. * sat_num / 100)**2
    if act_num is not None:
        act_weight = ((s1_avg_act - s2_avg_act) * 100/36. * act_num / 100)**2

    print(gpa_weight, sat_weight, act_weight)
    total = [gpa_weight, sat_weight, act_weight]
    total = [x for x in total if x is not None]
    dissimilarity = sum(total) if total != [] else None
    number = [x for x in [gpa_num, sat_num, act_num] if x is not None]
    number = max(number) if number != [] else None
    return dissimilarity, number


def compare_highschool(h1, h2, s1, s2):
    hs_dissimilarity = compare_highschool_grades(h1, h2)
    student_dissimilarity = compare_students(s1, s2)
    number_student_data = student_dissimilarity[1]  # number of data points
    student_dissimilarity = student_dissimilarity[0]  # actual score
    if student_dissimilarity is None:
        student_dissimilarity = 0
        number_student_data = 0  # don't use the data
        if hs_dissimilarity is None:
            return None

    hs_data_rate = 1 - min(0.35, number_student_data/100.0)
    if hs_dissimilarity is None:
        return student_dissimilarity # no high schools, so just take this
    score = (hs_dissimilarity*hs_data_rate +
             (1-hs_data_rate)*student_dissimilarity)
    return score
