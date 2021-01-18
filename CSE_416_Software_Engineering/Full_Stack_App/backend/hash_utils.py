import hmac
import hashlib
import random

sha256 = hashlib.sha256()


def generate_salt():
    letters = '1234567890!@#$%^&*()qwertyuiopasdfghjklzxcvbnmQWERUIOPASDF'\
              'GHJKLZXCVBNM[]{}\|;:<>,./?'
    salt = ''
    for x in range(0, 256):
        salt += letters[random.randint(0, len(letters)-1)]
    return salt


def hmac_hash(password, key):
    h = hmac.new(key.encode('utf-8'), password.encode('utf-8'), hashlib.sha256)
    return h.hexdigest()


def sha_hash(string):
    sha256.update(string.encode('utf-8'))
    return sha256.hexdigest()
