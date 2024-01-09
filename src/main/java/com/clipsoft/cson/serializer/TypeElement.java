package com.clipsoft.cson.serializer;


import com.clipsoft.cson.CSONArray;
import com.clipsoft.cson.CSONElement;
import com.clipsoft.cson.CSONObject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Method;

class TypeElement {


    protected static final TypeElement CSON_OBJECT;

    static {
        try {
            CSON_OBJECT = new TypeElement(CSONObject.class, CSONObject.class.getConstructor());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected static final TypeElement CSON_ARRAY;

    static {
        try {
            CSON_ARRAY = new TypeElement(CSONArray.class, CSONArray.class.getConstructor());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final Class<?> type;
    private final Constructor<?> constructor;
    private final ConcurrentHashMap<String, ObtainTypeValueInvoker> fieldValueObtaiorMap = new ConcurrentHashMap<>();

    private SchemaObjectNode schema;

    private final String comment;
    private final String commentAfter;
    private final Set<String> genericTypeNames = new HashSet<>();


    protected SchemaObjectNode getSchema() {
        if(schema == null) {
            schema = NodePath.makeSchema(this,null);
        }
        return schema;
    }

    private static Class<?> findNoAnonymousClass(Class<?> type) {
        if(!type.isAnonymousClass()) {
            return type;
        }
        Class<?> superClass = type.getSuperclass();
        if(superClass != null && superClass != Object.class) {
            return superClass;
        }
        Class<?>[] interfaces = type.getInterfaces();
        if(interfaces != null && interfaces.length > 0) {
            Class<?> foundCsonInterface = null;
            for(Class<?> interfaceClass : interfaces) {
                if(interfaceClass.getAnnotation(CSON.class) != null) {
                    if(foundCsonInterface != null) {
                        String allInterfaceNames = Arrays.stream(interfaces).map(Class::getName).reduce((a, b) -> a + ", " + b).orElse("");
                        throw new CSONSerializerException("Anonymous class " + type.getName() + "(implements  " + allInterfaceNames + "), implements multiple @CSON interfaces.  Only one @CSON interface is allowed.");
                    }
                    foundCsonInterface = interfaceClass;
                }
            }
            if(foundCsonInterface != null) {
                return foundCsonInterface;
            }
        }
        return type;
    }

    protected synchronized static TypeElement create(Class<?> type) {
        type = findNoAnonymousClass(type);

        if(CSONObject.class.isAssignableFrom(type)) {
            return CSON_OBJECT;
        }
        if(CSONArray.class.isAssignableFrom(type)) {
            return CSON_ARRAY;
        }
        checkCSONAnnotation(type);
        Constructor<?> constructor = null;
        try {
            constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException ignored) {}
        return new TypeElement(type, constructor);
    }



    protected Object newInstance() {
        try {
            if(constructor == null) {
                checkConstructor(type);
                return null;
            }
            return constructor.newInstance();
        } catch (Exception e) {
            throw new CSONSerializerException("Failed to create instance of " + type.getName(), e);
        }
    }



    protected boolean containsGenericType(String name) {
        return genericTypeNames.contains(name);
    }

    private TypeElement(Class<?> type, Constructor<?> constructor) {
        this.type = type;
        this.constructor = constructor;
        CSON cson = type.getAnnotation(CSON.class);
        if(cson != null) {
            String commentBefore = cson.comment();
            String commentAfter = cson.commentAfter();

            this.comment = commentBefore.isEmpty() ? null : commentBefore;
            this.commentAfter = commentAfter.isEmpty() ? null : commentAfter;
        } else {
            this.comment = null;
            this.commentAfter = null;
        }

        searchTypeParameters();
        searchMethodOfAnnotatedWithObtainTypeValue();

    }

    private void searchTypeParameters() {
        TypeVariable<?>[] typeVariables = this.type.getTypeParameters();
        for(TypeVariable<?> typeVariable : typeVariables) {
            genericTypeNames.add(typeVariable.getName());
        }
    }


    private void searchMethodOfAnnotatedWithObtainTypeValue() {
        searchMethodOfAnnotatedWithObtainTypeValue(this.type);
        Class<?>[] interfaces = this.type.getInterfaces();
        if(interfaces != null) {
            for(Class<?> interfaceClass : interfaces) {
                searchMethodOfAnnotatedWithObtainTypeValue(interfaceClass);
            }
        }

    }

    @SuppressWarnings("unchecked")
    private void searchMethodOfAnnotatedWithObtainTypeValue(Class<?> currentType) {
        // 이미 등록된 메서드를 덮어쓰지 않도록 하기 위해 HashSet을 사용한다.
        HashSet<String> methodNames = new HashSet<>();

        while(currentType != Object.class && currentType != null) {
            Method[] methods = currentType.getDeclaredMethods();
            for(Method method : methods) {
                ObtainTypeValue obtainTypeValue = method.getAnnotation(ObtainTypeValue.class);
                if(obtainTypeValue != null) {
                    String[] fieldNames = null;
                    String fieldNameValue = obtainTypeValue.value();
                    if(fieldNameValue != null && !fieldNameValue.isEmpty()) {
                        fieldNames = new String[]{fieldNameValue};
                    }
                    else {
                        fieldNames = obtainTypeValue.fieldNames();
                    }
                    if(fieldNames == null || fieldNames.length == 0) {
                        fieldNames = new String[]{SchemaMethod.getterNameFilter(method.getName())};
                    }

                    for(String fieldName : fieldNames) {
                        fieldName = fieldName.trim();

                        try {
                            type.getDeclaredField(fieldName);
                        } catch (NoSuchFieldException e) {
                            throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + type.getName() + "." + method.getName() + ". Field " + fieldName + " not found in " + type.getName());
                        }
                        Class<?> returnType = method.getReturnType();
                        Type genericReturnType = method.getGenericReturnType();
                        boolean genericType = false;
                        if(genericReturnType instanceof TypeVariable && genericTypeNames.contains(((TypeVariable<?>) genericReturnType).getName())) {
                            genericType = true;
                        }
                        if(!Types.isSingleType(Types.of(returnType)) && (returnType == void.class || returnType == Void.class || returnType == null || (!genericType && returnType.getAnnotation(CSON.class) == null))) {
                            throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + type.getName() + "." + method.getName() + ".  Return type must be a class annotated with @CSON");
                        }
                        int parameterCount = method.getParameterCount();
                        if(parameterCount > 0) {
                            Class<?> parameterType = method.getParameterTypes()[0];
                            //if(!CSONElement.class.isAssignableFrom(parameterType) && !CSONObject.class.isAssignableFrom(parameterType) && !CSONArray.class.isAssignableFrom(parameterType)) {
                            if(!parameterType.isAssignableFrom(CSONObject.class)) {
                                throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + type.getName() + "." + method.getName() + ". Parameter type only can be CSONObject");
                            }
                        }
                        if(parameterCount > 1) {
                            Class<?> parameterType = method.getParameterTypes()[1];
                            if(!parameterType.isAssignableFrom(CSONObject.class)) {
                                throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + type.getName() + "." + method.getName() + ". Parameter type only can be CSONObject");
                            }
                        }


                        if(parameterCount > 2) {
                            throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + type.getName() + "." + method.getName() + ".  Parameter count must be zero or one of CSONElement or CSONObject or CSONArray");
                        }
                        boolean ignoreError = obtainTypeValue.ignoreError();
                        fieldValueObtaiorMap.put(fieldName, new ObtainTypeValueInvoker(method,
                                returnType,
                                method.getParameterTypes(),
                                ignoreError));
                    }
                }
            }
            currentType= currentType.getSuperclass();
        }
    }

    Class<?> getType() {
        return type;
    }


    String getComment() {
        return comment;
    }

    String getCommentAfter() {
        return commentAfter;
    }


    private static boolean isCSONAnnotated(Class<?> type) {
        Class<?> superClass = type;
        while(superClass != Object.class && superClass != null) {
            CSON a = superClass.getAnnotation(CSON.class);
            if(a != null) {
                return true;
            }
            superClass = superClass.getSuperclass();
        }
        return false;

    }

    private static boolean isCSONAnnotatedOfInterface(Class<?> type) {
        Class<?>[] interfaces = type.getInterfaces();
        if(interfaces == null) {
            return false;
        }

        for(Class<?> interfaceClass : interfaces) {
            CSON a = interfaceClass.getAnnotation(CSON.class);
            if(a != null) {
                return true;
            }
            if(isCSONAnnotated(interfaceClass)) {
                return true;
            }

        }
        return false;
    }


    private static void checkCSONAnnotation(Class<?> type) {
         Annotation a = type.getAnnotation(CSON.class);
         if(a == null) {
             if(isCSONAnnotated(type) || isCSONAnnotatedOfInterface(type)) {
                 return;
             }
             if(type.isAnonymousClass()) {
                throw new CSONSerializerException("Anonymous class " + type.getName() + " is not annotated with @CSON");
             }
             throw new CSONSerializerException("Type " + type.getName() + " is not annotated with @CSON");
         }
    }

    private static void checkConstructor(Class<?> type) {
        Constructor<?> constructor = null;
        try {
            constructor = type.getDeclaredConstructor();
            if(constructor == null) {
                throw new CSONSerializerException("Type " + type.getName() + " has no default constructor");
            }
        } catch (NoSuchMethodException e) {
            throw new CSONSerializerException("Type " + type.getName() + " has invalid default constructor");
        }

    }

    ObtainTypeValueInvoker findObtainTypeValueInvoker(String fieldName) {
        return fieldValueObtaiorMap.get(fieldName);
    }



    static class ObtainTypeValueInvoker {

        private ObtainTypeValueInvoker(Method method, Class<?> returnType, Class<?>[] parameters, boolean ignoreError) {
            this.method = method;
            this.returnType = returnType;
            this.parameters = parameters;
            this.ignoreError = ignoreError;
        }

        private Method method;
        private Class<?> returnType;
        private Class<?>[] parameters;
        boolean ignoreError = false;


        public Object obtain(Object parents,CSONObject item, CSONObject all) {
            try {
                if(parameters == null || parameters.length == 0) {
                    return method.invoke(parents);
                } else if(parameters.length == 1) {
                    return method.invoke(parents, item);
                } else if(parameters.length == 2) {
                    return method.invoke(parents, item,all);
                } else {
                    throw new CSONSerializerException("Invalid @ObtainTypeValue method " + method.getName() + " of " + method.getDeclaringClass().getName() + ".  Parameter count must be zero or one of CSONElement or CSONObject or CSONArray");
                }
            } catch (Exception e) {
                if(ignoreError) {
                    return null;
                }
                throw new CSONSerializerException("Failed to invoke @ObtainTypeValue method " + method.getName() + " of " + method.getDeclaringClass().getName(), e);
            }
        }

    }




}
