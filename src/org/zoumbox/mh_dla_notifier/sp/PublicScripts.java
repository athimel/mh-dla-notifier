package org.zoumbox.mh_dla_notifier.sp;

/*
 * #%L
 * MountyHall DLA Notifier
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 - 2013 Zoumbox.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;
import org.zoumbox.mh_dla_notifier.MhDlaNotifierUtils;
import org.zoumbox.mh_dla_notifier.troll.Race;
import org.zoumbox.mh_dla_notifier.troll.Troll;
import org.zoumbox.mh_dla_notifier.utils.LogCallback;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class PublicScripts {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + PublicScripts.class.getSimpleName();

    protected static final Function<PublicScriptResult, Map<String, String>> SCRIPT_RESULT_TO_MAP = new Function<PublicScriptResult, Map<String, String>>() {
        @Override
        public Map<String, String> apply(PublicScriptResult input) {
            PublicScript script = input.getScript();
            List<String> lines = Lists.newArrayList(Splitter.on("\n").trimResults().omitEmptyStrings().split(input.getRaw()));
            Map<String, String> result = Maps.newLinkedHashMap();
            Set<String> types = script.getTypes();

            for (String line : lines) {
                Iterable<String> iterable = Splitter.on(";").trimResults().split(line); // Do not omit empty strings
                List<String> data = Lists.newArrayList(iterable);
                String suffix = "";

                if (types != null && !types.isEmpty()) {
                    final String typeToFind = data.get(0);
                    Optional<String> optional = Iterables.tryFind(types, Predicates.equalTo(typeToFind));
                    if (optional.isPresent()) {
                        suffix = typeToFind;
                    }
                }

                for (int i = 0; i < data.size() && i < script.properties.size(); i++) {
                    String key = script.properties.get(i);
                    if (!Strings.isNullOrEmpty(suffix)) {
                        key = key + suffix.substring(0, 1).toUpperCase() + suffix.substring(1).toLowerCase();
                    }
                    String value = data.get(i);
                    result.put(key, value);
                }
            }
//            case Vue:
//                int monstresStart = lines.indexOf("#DEBUT MONSTRES");
//                int monstresEnd = lines.indexOf("#FIN MONSTRES");
//                List<String> monstresList = lines.subList(monstresStart + 1, monstresEnd);
//                String monstres = Joiner.on("\n").join(monstresList);
//                result.put(PublicScriptProperties.MONSTRES.name(), monstres);
//
//                int trollsStart = lines.indexOf("#DEBUT TROLLS");
//                int trollsEnd = lines.indexOf("#FIN TROLLS");
//                List<String> trollsList = lines.subList(trollsStart + 1, trollsEnd);
//                String trolls = Joiner.on("\n").join(trollsList);
//                result.put(PublicScriptProperties.TROLLS.name(), trolls);
//                break;
            return result;
        }
    };


    public static void pushToTroll(Troll troll, Map<String, String> propertiesFetched, LogCallback log) {
        List<Method> methods = Arrays.asList(Troll.class.getMethods());
        for (Map.Entry<String, String> entry : propertiesFetched.entrySet()) {
            try {
                String name = entry.getKey();
                final String expectedMethodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
                Optional<Method> optional = Iterables.tryFind(methods, new Predicate<Method>() {
                    @Override
                    public boolean apply(Method input) {
                        return expectedMethodName.equals(input.getName());
                    }
                });
                if (optional.isPresent()) {
//
                    Method method = optional.get();
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Preconditions.checkState(parameterTypes.length == 1, "Unexpected parameters length for method: " + expectedMethodName);

                    Class<?> type = parameterTypes[0];
                    String stringValue = entry.getValue();
                    Object value = stringValue;
                    if (int.class.equals(type)) {
                        value = Integer.parseInt(stringValue);
                    } else if (boolean.class.equals(type)) {
                        value = "1".equals(stringValue);
                    } else if (double.class.equals(type)) {
                        value = Double.parseDouble(stringValue);
                    } else if (Race.class.equals(type)) {
                        value = Race.valueOf(stringValue);
                    } else if (Date.class.equals(type)) {
                        value = MhDlaNotifierUtils.parseDate(stringValue);
                    }

                    method.invoke(troll, value);

                } else {
                    log.w(TAG, "Ignored property (unwritable): " + name);
                }
            } catch (IllegalStateException ise) {
                log.e(TAG, "An exception occured", ise);
            } catch (IllegalAccessException iae) {
                log.e(TAG, "An exception occured", iae);
            } catch (InvocationTargetException ite) {
                log.e(TAG, "An exception occured", ite);
            }
        }

    }

    public static void pushToTroll(Troll troll, PublicScriptResult publicScriptResult, LogCallback log) {
        String scriptName = publicScriptResult.getScript().name();
        log.i(TAG, String.format("%s result [raw=%s]", scriptName, publicScriptResult.getRaw()));
        Map<String, String> map = PublicScripts.SCRIPT_RESULT_TO_MAP.apply(publicScriptResult);
        log.i(TAG, String.format("%s result [map=%s]", scriptName, map));
        PublicScripts.pushToTroll(troll, map, log);
        log.i(TAG, String.format("%s result [troll=%s]", scriptName, troll));
    }

}
