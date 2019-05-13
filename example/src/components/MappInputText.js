import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import {
    StyleSheet,
    View,
    Platform,
    TouchableOpacity,
} from 'react-native';
import Reinput from 'reinput'
import styleVars from './variables';

const defaultContainerHeight = 80; // Wrapper height
const defaultTextHeight = 16; // Font size
const androidInputAdjustment = 60; // Magic number
const androidContainerAdjustment = 34; // Magic number

export default class InputText extends PureComponent {
    static propTypes = {
        style: PropTypes.object,
        label: PropTypes.string,
        placeholder: PropTypes.string,
        error: PropTypes.bool,
        errorMessage: PropTypes.string,
        multiline: PropTypes.bool,
        register: PropTypes.func,
        secureTextEntry: PropTypes.bool,
    }

    static defaultProps = {
        style: {},
        label: '',
        placeholder: '',
        error: false,
        errorMessage: '',
        multiline: false,
        register: () => void(0),
        secureTextEntry: false,
    }
    constructor(props) {
        super(props);

        this.state = {
            password: props.secureTextEntry,
            eyeIcon: 'eye-off',
            containerHeight: defaultContainerHeight,
        }
    }

    handleContentSizeChange = ({ nativeEvent }) => {
        if (!this.props.multiline) return;

        let containerHeight = defaultContainerHeight - defaultTextHeight + nativeEvent.contentSize.height;

        if (Platform.OS === 'android') {
            containerHeight = containerHeight - androidContainerAdjustment;
        }

        if (containerHeight !== this.state.containerHeight) {
            this.setState({ containerHeight });
        }
    }

    handleEyeIconPress = () => {
        this.setState(prevState => ({
            password: !prevState.password,
            eyeIcon: prevState.eyeIcon === 'eye-off' ? 'eye' : 'eye-off',
        }));
    }

    render() {
        const {
            style,
            label,
            placeholder,
            error,
            errorMessage,
            multiline,
            register,
            secureTextEntry,
            ...otherProps
        } = this.props;

        const {
            password,
            eyeIcon,
            containerHeight,
        } = this.state;

        return (
            <View style={{width:"100%"}}>

                    <Reinput
                        register={register}
                        labelActiveScale={1}
                        height={multiline && Platform.OS === 'android' ? containerHeight - androidInputAdjustment : undefined}
                        label={label}
                        placeholder={placeholder}
                        error={errorMessage || (error ? ' ' : '')}
                        color={"red"}
                        placeholderColor={styleVars.inputTextPlaceholderColor}
                        labelColor={styleVars.inputTextPlaceholderColor}
                        labelActiveColor={"red"}
                        underlineColor={styleVars.inputTextPlaceholderColor}
                        underlineActiveColor={"red"}
                        errorColor={"red"}
                        onContentSizeChange={this.handleContentSizeChange}
                        multiline={multiline}
                        paddingTop={error ? 52 : 20}
                        paddingRight={secureTextEntry ? 20 : 0}
                        secureTextEntry={password}
                        {...otherProps}
                    />

            </View>
        );
    }
}
const styles = StyleSheet.create({
    eyeIconContainer: {
        position: 'absolute',
        top: 20,
        right: 0,
        paddingVertical: 10,
        paddingLeft: 10,
        marginTop: Platform.select({ android: 3, ios: 0 }),
    },
});
