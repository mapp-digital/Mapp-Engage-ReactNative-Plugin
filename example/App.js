/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {Component} from 'react';
import {
    StyleSheet,
    Text,
    View,
    Platform,
    ScrollView
} from 'react-native';
import MappButton from './src/components/MappButton'
import MappInputText from './src/components/MappInputText'
const instructions = Platform.select({
    ios: 'Press Cmd+R to reload,\n' + 'Cmd+D or shake for dev menu',
    android:
    'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});
/**
 * Created by Aleksandar Marinkovic on 5/16/19.
 * Copyright (c) 2019 MAPP.
 */
type Props = {};
export default class App extends Component<Props> {
    state = {
        alias: '',
        removeAttribute:'',
        addAttribute:'',
        getAttribute:'',
        removeTags:'',
        setTags:'',
        defaultError: false,
        defaultErrorMessage: '',
    }

    handleTextChange = (type) => (text) => {
        this.setState({ [type]: text });
    }

    render() {
        const { state } = this;
        return (
            <ScrollView>
                <View style={styles.container}>
                    <Text style={styles.welcome}>Welcome to React Native Mapp plugin!</Text>
                    <MappInputText
                        maxLength={255}
                        label="Set Device Alias"
                        autoCorrect={false}
                        autoCapitalize="none"
                        value={state.aliasState}
                        onChangeText={this.handleTextChange('aliasState')}
                        error={state.defaultError}
                        errorMessage={state.defaultErrorMessage}
                    />
                    <MappButton
                        text={"Set Device Alias"}
                        onPress={this.setAlias}
                    />
                    <MappButton
                        text={"Get Device Alias"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"Device Information"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"Is Puh Enabled"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"Opt in"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"Opt out"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"Start Geo"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"Stop Geo"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"Fetch inbox messages"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"In App: App Open"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"In App: App Feedback"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"In App: App Discount"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"In App: App Promo"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"Fetch Multiple Messages"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"Get Tags"}
                        onPress={this.setAlias}/>

                    <MappInputText
                        maxLength={255}
                        label="Set Tags"
                        autoCorrect={false}
                        autoCapitalize="none"
                        value={state.setTags}
                        onChangeText={this.handleTextChange('setTags')}
                        error={state.defaultError}
                        errorMessage={state.defaultErrorMessage}
                    />
                    <MappButton
                        text={"Set Tags"}
                        onPress={this.setAlias}/>

                    <MappInputText
                        maxLength={255}
                        label="Remove Tags"
                        autoCorrect={false}
                        autoCapitalize="none"
                        value={state.removeTags}
                        onChangeText={this.handleTextChange('removeTags')}
                        error={state.defaultError}
                        errorMessage={state.defaultErrorMessage}
                    />
                    <MappButton
                        text={"Remove Tag"}
                        onPress={this.setAlias}/>
                    <MappInputText
                        maxLength={255}
                        label="Set Attribute"
                        autoCorrect={false}
                        autoCapitalize="none"
                        value={state.setAttribute}
                        onChangeText={this.handleTextChange('setAttribute')}
                        error={state.defaultError}
                        errorMessage={state.defaultErrorMessage}
                    />
                    <MappButton
                        text={"Set Attribute"}
                        onPress={this.setAlias}/>
                    <MappInputText
                        maxLength={255}
                        label="Get Attribute"
                        autoCorrect={false}
                        autoCapitalize="none"
                        value={state.getAttribute}
                        onChangeText={this.handleTextChange('getAttribute')}
                        error={state.defaultError}
                        errorMessage={state.defaultErrorMessage}
                    />
                    <MappButton
                        text={"Get Attribute"}
                        onPress={this.setAlias}/>
                    <MappInputText
                        maxLength={255}
                        label="Remove Attribute"
                        autoCorrect={false}
                        autoCapitalize="none"
                        value={state.removeAttribute}
                        onChangeText={this.handleTextChange('removeAttribute')}
                        error={state.defaultError}
                        errorMessage={state.defaultErrorMessage}
                    />
                    <MappButton
                        text={"Remove Attribute"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"Remove Badge Number"}
                        onPress={this.setAlias}/>
                    <MappButton
                        text={"Lock Orientation"}
                        onPress={this.setAlias}/>
                </View>
            </ScrollView>
        );
    }

    setAlias = () => {

    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        marginHorizontal:20,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#F5FCFF',
    },
    welcome: {
        fontSize: 20,
        textAlign: 'center',
        margin: 10,
    },
    instructions: {
        textAlign: 'center',
        color: '#333333',
        marginBottom: 5,
    },
});
