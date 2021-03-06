package ru.yandex.qatools.allure.data.plugins

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Inject
import groovy.transform.EqualsAndHashCode
import org.junit.Test

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 07.02.15
 */
class PluginManagerTest {

    @Test
    void shouldNotFailIfLoadNull() {
        def loader = [loadPlugins: { null }] as PluginLoader
        new PluginManager(loader)
    }

    @Test
    void shouldNotFailIfProcessNull() {
        def loader = [loadPlugins: { [] }] as PluginLoader
        def manager = new PluginManager(loader)

        manager.prepare(null)
        manager.process(null)
        manager.getData(null);
    }

    @Test
    void shouldNotFailIfNoPlugins() {
        def loader = [loadPlugins: { [] }] as PluginLoader
        def manager = new PluginManager(loader)

        manager.prepare(new Object())
        manager.process(new ArrayList())
        manager.getData(Integer);
    }

    @Test
    void shouldNotFailIfNullPlugins() {
        def loader = [loadPlugins: { [null] }] as PluginLoader
        def manager = new PluginManager(loader)

        manager.prepare(new Object())
        manager.process(new ArrayList())
        manager.getData(Integer);
    }

    @Test
    void shouldDoNothingIfNoPluginForTypePreparedObject() {
        def loader = [loadPlugins: { [new SomePreparePlugin()] }] as PluginLoader
        def manager = new PluginManager(loader)

        Integer number = 4;
        manager.prepare(number)

        assert number == 4
    }

    @Test
    void shouldChangePreparedObjects() {
        def plugin1 = new SomePreparePlugin(suffix: "_PLUGIN1")
        def plugin2 = new SomePreparePlugin(suffix: "_PLUGIN2")
        def loader = [loadPlugins: { [plugin1, plugin2] }] as PluginLoader
        def manager = new PluginManager(loader)

        def object1 = new SomeObject(someValue: "object1")
        manager.prepare(object1)
        assert object1.someValue == "object1_PLUGIN1_PLUGIN2"

        def object2 = new SomeObject(someValue: "object2")
        manager.prepare(object2)
        assert object2.someValue == "object2_PLUGIN1_PLUGIN2"
    }

    @Test
    void shouldNotChangeProcessedObjects() {
        def plugin1 = new SomeProcessPlugin(suffix: "_PLUGIN1")
        def plugin2 = new SomeProcessPlugin(suffix: "_PLUGIN2")
        def loader = [loadPlugins: { [plugin1, plugin2] }] as PluginLoader
        def manager = new PluginManager(loader)

        def object1 = new SomeObject(someValue: "object1")
        manager.process(object1)
        assert object1.someValue == "object1"

        def object2 = new SomeObject(someValue: "object2")
        manager.process(object2)
        assert object2.someValue == "object2"
    }

    @Test
    void shouldUpdateDataWhenProcessObjects() {
        def plugin1 = new SomeProcessPlugin(suffix: "_PLUGIN1")
        def plugin2 = new SomeProcessPlugin(suffix: "_PLUGIN2")
        def loader = [loadPlugins: { [plugin1, plugin2] }] as PluginLoader
        def manager = new PluginManager(loader)

        def object1 = new SomeObject(someValue: "object1")
        manager.process(object1)
        def object2 = new SomeObject(someValue: "object2")
        manager.process(object2)

        def data = manager.getData(SomeObject)
        assert data
        assert data.size() == 4
        assert data.collect { item -> (item.data as SomeObject).someValue }.containsAll([
                "object1_PLUGIN1",
                "object1_PLUGIN2",
                "object2_PLUGIN1",
                "object2_PLUGIN2"
        ])
        assert data.collect { item -> item.name }.containsAll([
                "name_PLUGIN1",
                "name_PLUGIN2",
                "name_PLUGIN1",
                "name_PLUGIN2"
        ])
    }

    @Test
    void shouldInjectMembersToPlugins() {
        def plugin = new SomePluginWithInjection()
        def loader = [loadPlugins: { [plugin] }] as PluginLoader
        def injectable = new SomeInjectable(value: "some nice value")
        def injector = new SomeInjector(injectable: injectable)
        //noinspection GroovyUnusedAssignment
        def manager = new PluginManager(loader, Guice.createInjector(injector))

        plugin.injectable == injectable
    }

    @EqualsAndHashCode
    class SomeInjectable {
        String value
    }

    class SomeInjector extends AbstractModule {

        SomeInjectable injectable;

        @Override
        protected void configure() {
            bind(SomeInjectable).toInstance(injectable)
        }
    }

    class SomePluginWithInjection extends SomePlugin implements PreparePlugin<SomeObject> {

        @Inject
        SomeInjectable injectable;

        @Override
        void prepare(SomeObject data) {
            //do nothing
        }
    }

    class SomePreparePlugin extends SomePlugin implements PreparePlugin<SomeObject> {
        def suffix = "_SUFFIX";

        @Override
        void prepare(SomeObject data) {
            data.someValue += suffix
        }
    }

    class SomeProcessPlugin extends SomePlugin implements ProcessPlugin<SomeObject> {
        def suffix = "_SUFFIX"
        List<PluginData> pluginData = []

        @Override
        void process(SomeObject data) {
            data.someValue += suffix
            pluginData.add(new PluginData("name" + suffix, data))
        }

        @Override
        List<PluginData> getPluginData() {
            return pluginData
        }
    }

    abstract class SomePlugin implements Plugin<SomeObject> {
        @Override
        Class<SomeObject> getType() {
            return SomeObject
        }
    }

    @EqualsAndHashCode
    class SomeObject {
        String someValue;
    }
}
