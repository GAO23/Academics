from mongoengine import Document, StringField, BooleanField, FloatField
from mongoengine import IntField, ReferenceField, ListField, DynamicField
from mongoengine import CASCADE


class Account(Document):
    username = StringField(required=True, max_length=32, unique=True)
    hashed_password = StringField(required=True, max_length=256)
    salt = StringField(required=True, max_length=256)
    type = StringField(choices=('Student', 'Admin'), required=True)


class StudentProfile(Document):
    student = ReferenceField(Account, required=True, unique=True,
                             reverse_delete_rule=CASCADE)
    gpa = FloatField(min_value=0.0, max_value=5.0)
    # unweighted GPA goes up to 5.0?
    grades = DynamicField(default=dict)
    residence_state = StringField(max_length=2)  # store state acronym.
    high_school_name = StringField()
    high_school_city = StringField()
    high_school_state = StringField(max_length=2)
    college_class = IntField(min_value=0, max_value=3000)
    major_1 = StringField()
    major_2 = StringField()


class College(Document):
    name = StringField(required=True, max_length=100, unique=True)
    # Longest college name in colleges.txt is 50 characters:
    # SUNY College of Environmental Science and Forestry
    city = StringField(max_length=30)  # , required=True)
    state = StringField(max_length=2)  # , required=True)
    region = StringField()  # required=True, max_length=10)
    institution = StringField()  # required=True, max_length=20)
    admission_rate = FloatField()
    completion_rate = FloatField()
    size = IntField()  # required=True, max_length=6)
    median_debt = StringField()  # required=True, max_length=6)
    salary = StringField()
    in_cost = IntField()
    out_cost = IntField()
    avg_gpa = FloatField(min_value=0.0, max_value=5.0)
    majors = ListField()
    ranking = IntField(min_value=1, max_value=601)  # StringField(max_length=4)
    avg_sat_math = IntField(max_length=3)
    sat_math_25 = IntField(max_length=3)
    sat_math_75 = IntField(max_length=3)
    avg_sat_ebrw = IntField(max_length=3)
    sat_ebrw_25 = IntField(max_length=3)
    sat_ebrw_75 = IntField(max_length=3)
    avg_act_composite = IntField(max_length=2)
    act_composite_25 = IntField(max_length=2)
    act_composite_75 = IntField(max_length=2)


class Application(Document):
    # TODO: reference the correct student class?
    # id will be built from the hash of the student and college
    ID = StringField(required=True, unique=True)
    # reference the student this application belongs to
    # if the student is deleted, cascade and delete this application
    student = ReferenceField(StudentProfile, required=True,
                             reverse_delete_rule=CASCADE)
    # unique with cannot work with reference fields
    college = ReferenceField(College, required=True,
                             reverse_delete_rule=CASCADE)
    timestamp = StringField(required=True)
    status = StringField(choices=('Pending', 'Accepted', 'Denied', 'Deferred',
                                  'Wait-listed', 'Withdrawn'), required=True)
    verification = StringField(choices=('Approved', 'Pending', 'Denied'), required=True)


class HighSchool(Document):
    name = StringField(reqired=True)
    city = StringField(reqired=True)
    state = StringField(reqired=True, unqiue_with=(name, city))
    reading_prof = IntField(min_value=0, max_value=100)
    math_prof = IntField(mind_value=0, max_value=100)
    grad_rate = IntField(mind_value=0, max_value=100)
    avg_sat = IntField()  # SAT might change again
    avg_act = IntField(min_value=0, max_value=36)
    ap_enroll = IntField(min_value=0, max_value=100)
    scraped = BooleanField(required=True)

class Student(Document):
    profile = ReferenceField(StudentProfile)
    account = ReferenceField(Account)
