

print("importando fake")


a =0

def fake():
    global a
    a = a+1
    print("fake " + str(a) )
    return None